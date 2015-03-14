package com.kaaprotech.satu.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.Module;

/**
 * Created by jwhiting on 13/03/2015.
 */
public class SatuModule extends Module {
    @Override
    public String getModuleName() {
        return "SatuModule";
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        setupContext.addDeserializers(new SatuDeserializers());
        setupContext.addTypeModifier(new SatuTypeModifier());
    }
}
