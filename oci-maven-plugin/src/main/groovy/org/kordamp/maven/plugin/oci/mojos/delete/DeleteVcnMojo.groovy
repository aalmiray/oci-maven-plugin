/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.maven.plugin.oci.mojos.delete

import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.DeleteVcnRequest
import com.oracle.bmc.core.requests.GetVcnRequest
import com.oracle.bmc.core.requests.ListVcnsRequest
import groovy.transform.CompileStatic
import org.apache.maven.plugins.annotations.Mojo
import org.kordamp.maven.plugin.oci.mojos.AbstractOCIMojo
import org.kordamp.maven.plugin.oci.mojos.traits.CompartmentIdAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.OptionalVcnIdAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.OptionalVcnNameAwareTrait
import org.kordamp.maven.plugin.oci.mojos.traits.WaitForCompletionAwareTrait

import static org.kordamp.maven.StringUtils.isBlank
import static org.kordamp.maven.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
@Mojo(name = 'delete-vcn')
class DeleteVcnMojo extends AbstractOCIMojo implements CompartmentIdAwareTrait,
    OptionalVcnIdAwareTrait,
    OptionalVcnNameAwareTrait,
    WaitForCompletionAwareTrait {
    @Override
    protected void executeGoal() {
        validateVcnId()

        if (isBlank(getVcnId()) && isBlank(getVcnName())) {
            throw new IllegalStateException("Missing value for either 'vcnId' or 'vcnName' in $path")
        }

        VirtualNetworkClient client = createVirtualNetworkClient()

        // TODO: check if vcn exists
        // TODO: check is vcn is in a 'deletable' state

        if (isNotBlank(getVcnId())) {
            Vcn vcn = client.getVcn(GetVcnRequest.builder()
                .vcnId(getVcnId())
                .build())
                .vcn

            if (vcn) {
                setVcnName(vcn.displayName)
                deleteVcn(client, vcn)
            }
        } else {
            validateCompartmentId()

            client.listVcns(ListVcnsRequest.builder()
                .compartmentId(getCompartmentId())
                .displayName(getVcnName())
                .build())
                .items.each { vcn ->
                setVcnId(vcn.id)
                deleteVcn(client, vcn)
            }
        }
    }

    private void deleteVcn(VirtualNetworkClient client, Vcn vcn) {
        println("Deleting Vcn '${vcn.displayName}' with id ${vcn.id}")
        client.deleteVcn(DeleteVcnRequest.builder()
            .vcnId(vcn.id)
            .build())

        if (isWaitForCompletion()) {
            println("Waiting for Vcn to be ${state('Terminated')}")
            client.waiters
                .forVcn(GetVcnRequest.builder().vcnId(vcn.id).build(),
                    Vcn.LifecycleState.Terminated)
                .execute()
        }
    }
}
