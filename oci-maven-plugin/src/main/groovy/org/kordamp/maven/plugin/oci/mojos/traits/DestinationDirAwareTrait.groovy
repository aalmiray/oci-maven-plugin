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
package org.kordamp.maven.plugin.oci.mojos.traits

import groovy.transform.CompileStatic
import org.apache.maven.plugins.annotations.Parameter
import org.kordamp.maven.plugin.oci.mojos.interfaces.PathAware

import static org.kordamp.maven.PropertyUtils.directoryProperty

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
trait DestinationDirAwareTrait implements PathAware {
    @Parameter(property = 'oci.destination.dir', name = 'destinationdir')
    File destinationDir

    File getDestinationDir() {
        directoryProperty('OCI_DESTINATION_DIR', 'oci.destination.dir', this.@destinationDir)
    }

    void validateDestinationDir() {
        if (!destinationDir) {
            throw new IllegalStateException("Missing value for 'destinationDir' in $path")
        }
    }
}
