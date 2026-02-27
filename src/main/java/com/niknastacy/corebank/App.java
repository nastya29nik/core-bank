package com.niknastacy.corebank;

import com.niknastacy.corebank.cli.ConsoleListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan
@PropertySource("classpath:application.properties")
public class App {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        ConsoleListener consoleListener = context.getBean(ConsoleListener.class);
        consoleListener.run();
    }
}