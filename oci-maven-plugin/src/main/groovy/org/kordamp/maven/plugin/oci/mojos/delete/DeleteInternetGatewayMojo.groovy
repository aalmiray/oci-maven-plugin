/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2022 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.maven.plugin.oci.mojos.delete

import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.InternetGateway
import com.oracle.bmc.core.requests.DeleteInternetGatewayRequest
import com.oracle.bmc.core.requests.GetInternetGatewayRequest
import com.oracle.bmc.core.requests.ListInternetGatewaysRequest
import groovy.transform.CompileStatic
import org.apache.maven.plugins.annotations.Mojo
import org.kordamp.maven.plugin.oci.mojos.AbstractOCIMojo
import org.kordamp.maven.plugin.oci.mojos.traits.CompartmentIdAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.OptionalInternetGatewayIdAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.OptionalInternetGatewayNameAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.VcnIdAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.WaitForCompletionAwareTrait

import static org.kordamp.maven.StringUtils.isBlank
import static org.kordamp.maven.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
@Mojo(name = 'delete-internet-gateway')
class DeleteInternetGatewayMojo extends AbstractOCIMojo implements CompartmentIdAwareTrait,
    VcnIdAwareTrait,
    OptionalInternetGatewayIdAwareTrait,
    OptionalInternetGatewayNameAwareTrait,
    WaitForCompletionAwareTrait {

    @Override
    protected List<String> resolveInterpolationProperties() {
        [
            'compartmentId',
            'vcnId',
            'internetGatewayId',
            'internetGatewayName'
        ]
    }

    @Override
    protected void executeGoal() {
        validateInternetGatewayId()

        if (isBlank(getInternetGatewayId()) && isBlank(getInternetGatewayName())) {
            throw new IllegalStateException("Missing value for either 'internetGatewayId' or 'internetGatewayName' in $path")
        }

        VirtualNetworkClient client = createVirtualNetworkClient()

        // TODO: check if gateway exists
        // TODO: check is gateway is in a 'deletable' state

        if (isNotBlank(getInternetGatewayId())) {
            InternetGateway internetGateway = client.getInternetGateway(GetInternetGatewayRequest.builder()
                .igId(getInternetGatewayId())
                .build())
                .internetGateway

            if (internetGateway) {
                setInternetGatewayName(internetGateway.displayName)
                deleteInternetGateway(client, internetGateway)
            }
        } else {
            validateCompartmentId()
            validateVcnId()

            client.listInternetGateways(ListInternetGatewaysRequest.builder()
                .compartmentId(getCompartmentId())
                .vcnId(getVcnId())
                .displayName(getInternetGatewayName())
                .build())
                .items.each { internetGateway ->
                setInternetGatewayId(internetGateway.id)
                deleteInternetGateway(client, internetGateway)
            }
        }
    }

    private void deleteInternetGateway(VirtualNetworkClient client, InternetGateway internetGateway) {
        println("Deleting InternetGateway '${internetGateway.displayName}' with id ${internetGateway.id}")

        client.deleteInternetGateway(DeleteInternetGatewayRequest.builder()
            .igId(internetGateway.id)
            .build())

        if (isWaitForCompletion()) {
            println("Waiting for InternetGateway to be ${state('Terminated')}")
            client.waiters
                .forInternetGateway(GetInternetGatewayRequest.builder()
                    .igId(internetGateway.id).build(),
                    InternetGateway.LifecycleState.Terminated)
                .execute()
        }

        // TODO: remove from vcn routing table
    }
}
