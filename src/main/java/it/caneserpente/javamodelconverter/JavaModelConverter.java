package it.caneserpente.javamodelconverter;

import it.caneserpente.javamodelconverter.builder.ClassConverterDirector;
import it.caneserpente.javamodelconverter.converter.base.AClassConverter;

import java.util.List;

public class JavaModelConverter {

    public static final boolean DEBUG = true;

    /**
     * app entry point
     * @param args
     */
    public static void main(String[] args) {

        // scans directory for .java files and build it
        List<String> classList = new ClassListScanner().scanForClasses();

        if (DEBUG) {
            classList.stream().forEach(c -> System.out.println(c));
        }

        // create desired class converter
        AClassConverter classConverter = new ClassConverterDirector(classList).construct();

        // convert classes
        classConverter.convertClassList(classList);
    }
}