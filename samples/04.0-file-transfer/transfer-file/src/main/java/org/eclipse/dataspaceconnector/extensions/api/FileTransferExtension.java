/*
 *  Copyright (c) 2021 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.DataTransferExecutorServiceContainer;
import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.policy.PolicyDefinition;
import org.eclipse.dataspaceconnector.spi.policy.store.PolicyDefinitionStore;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;

import java.nio.file.Path;

public class FileTransferExtension implements ServiceExtension {

    public static final String USE_POLICY = "use-eu";
    private static final String EDC_ASSET_PATH = "edc.samples.04.asset.path";
    @Inject
    private ContractDefinitionStore contractStore;
    @Inject
    private AssetLoader loader;
    @Inject
    private PipelineService pipelineService;
    @Inject
    private DataTransferExecutorServiceContainer executorContainer;
    @Inject
    private PolicyDefinitionStore policyStore;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        var sourceFactory = new FileTransferDataSourceFactory();
        pipelineService.registerFactory(sourceFactory);

        var sinkFactory = new FileTransferDataSinkFactory(monitor, executorContainer.getExecutorService(), 5);
        pipelineService.registerFactory(sinkFactory);

        var policy = createPolicy();
        policyStore.save(policy);

        registerDataEntries(context);
        registerContractDefinition(policy.getUid());

        context.getMonitor().info("File Transfer Extension initialized!");
    }

    private PolicyDefinition createPolicy() {
        var usePermission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .build();

        return PolicyDefinition.Builder.newInstance()
                .uid(USE_POLICY)
                .policy(Policy.Builder.newInstance()
                        .permission(usePermission)
                        .build())
                .build();
    }

    private void registerDataEntries(ServiceExtensionContext context) {
        var assetPathSetting = context.getSetting(EDC_ASSET_PATH, "/tmp/provider/test-document.txt");
        var assetPath = Path.of(assetPathSetting);

        var dataAddress = DataAddress.Builder.newInstance()
                .property("type", "File")
                .property("path", assetPath.getParent().toString())
                .property("filename", assetPath.getFileName().toString())
                .build();

        var assetId = "test-document";
        var asset = Asset.Builder.newInstance().id(assetId).build();

        loader.accept(asset, dataAddress);
    }

    private void registerContractDefinition(String uid) {
        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(uid)
                .contractPolicyId(uid)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance()
                        .whenEquals(Asset.PROPERTY_ID, "test-document")
                        .build())
                .build();

        contractStore.save(contractDefinition);
    }
}
