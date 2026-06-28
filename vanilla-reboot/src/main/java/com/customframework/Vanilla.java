package com.customframework;

import com.customframework.context.VanillaContext;
import com.customframework.log.BannerPrinter;
import com.customframework.log.ResourceBannerPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vanilla {
    private static final Logger log = LoggerFactory.getLogger(Vanilla.class);

    public static VanillaContext start(Class<?> primarySource) {
            BannerPrinter bannerPrinter = pickBannerPrinter();
            bannerPrinter.printBanner(System.out);

            log.info("\u001B[32m Starting my vanilla framework for "
                    + primarySource.getSimpleName() + "! ... \u001B[0m");

            VanillaContext context = new VanillaContext(primarySource);

            log.info("\u001B[32m Context initialized successfully! \u001B[0m");

            return context;
    }




    private static BannerPrinter pickBannerPrinter() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if(loader.getResource("bannerd.txt") != null) {
            return new ResourceBannerPrinter("banner.txt",loader);
        }

        return new ResourceBannerPrinter("default-banner.txt", Vanilla.class.getClassLoader());
    }
}
