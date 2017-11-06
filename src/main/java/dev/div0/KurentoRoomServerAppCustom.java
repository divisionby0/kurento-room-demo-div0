package dev.div0;

import org.kurento.room.KurentoRoomServerApp;
import org.kurento.room.NotificationRoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

public class KurentoRoomServerAppCustom extends KurentoRoomServerApp{

    private static final Logger log = LoggerFactory.getLogger(KurentoRoomServerAppCustom.class);

    @Override
    @Bean
    @ConditionalOnMissingBean
    public NotificationRoomManagerCustom roomManager() {
        return new NotificationRoomManagerCustom(notificationService(), kmsManager());
    }

    public static ConfigurableApplicationContext start(String[] args) {
        log.info("Using /dev/urandom for secure random generation");
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        return SpringApplication.run(KurentoRoomServerAppCustom.class, args);
    }
}
