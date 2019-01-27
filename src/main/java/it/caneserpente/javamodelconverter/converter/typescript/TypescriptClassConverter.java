package it.caneserpente.javamodelconverter.converter.typescript;

import it.caneserpente.javamodelconverter.ApplicationConfig;
import it.caneserpente.javamodelconverter.converter.base.AClassConverter;
import it.caneserpente.javamodelconverter.converter.base.ADatatypeConverter;
import it.caneserpente.javamodelconverter.model.JMCClass;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;

public class TypescriptClassConverter extends AClassConverter {

    private final String TS_EXT = ".ts";

    /**
     * if true interfaces are generated instead of classes
     */
    private boolean generateInterfaces;

    /**
     * contains type name to generate
     */
    private String typeToGenerate;

    /**
     * if true follows angular coding style
     */
    private boolean angularCodingStyle;

    public TypescriptClassConverter() {
        super();
        this.loadConfig();
    }

    @Override
    protected void loadConfig() {
        this.generateInterfaces = ApplicationConfig.getInstance().isGenerateInterface();
        this.typeToGenerate = this.generateInterfaces ? "interface" : "class";
        this.angularCodingStyle = ApplicationConfig.getInstance().isAngularCodingStyle();
    }


    @Override
    protected JMCClass convertClassName(@Nullable JMCClass clz) {
        if (null != clz) {
            clz.setConvertedClassName(clz.getClazz().getSimpleName());
        }
        return clz;
    }


    @Override
    protected String createClassFileName(JMCClass clz) {
        if (this.angularCodingStyle) {
            // split each found uppercase
            String[] clzNameToken = clz.getConvertedClassName().split("(?=\\p{Lu})");
            // example: pojo-test.model.ts
            return String.join("-", clzNameToken).toLowerCase() + ".model" + TS_EXT;
        } else {
            return clz.getConvertedClassName() + TS_EXT;
        }
    }


    @Override
    protected void writeGeneratedClass(@Nullable JMCClass clz) {

        StringBuilder sb = new StringBuilder();

        // import
        this.writeImports(clz, sb);

        // open class block
        sb.append("export " + this.typeToGenerate + " " + clz.getConvertedClassName() + " {").append("\n\n");

        // fields
        clz.getFieldList().stream().forEach(f -> sb.append(f.getConvertedFieldStm()));

        // constructor (only if class generation)
        if (! this.generateInterfaces) {

            sb.append("\n\n" + clz.getConvertedConstructorStart());
            clz.getFieldList().stream().forEach(f -> sb.append(f.getConvertedContructorFieldStm()));
            sb.append(clz.getConvertedConstructorEnd());
        }

        // close class block
        sb.append("}");

        try (PrintWriter writer = new PrintWriter(new File(super.outputDir.getAbsolutePath() + System.getProperty("file.separator") + this.createClassFileName(clz)))) {
            writer.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


}
