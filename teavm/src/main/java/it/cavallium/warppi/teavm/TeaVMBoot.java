package it.cavallium.warppi.teavm;

import it.cavallium.warppi.boot.Boot;

public class TeaVMBoot {

	public static void main(final String[] args) throws Exception {
		Boot.boot(new TeaVMPlatform(), args);
	}

}