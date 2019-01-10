

package it.caneserpente.javamodelconverter.converter.base;

import com.sun.istack.internal.Nullable;
import it.caneserpente.javamodelconverter.JavaFieldReader;
import it.caneserpente.javamodelconverter.model.JMCClass;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public abstract class AClassConverter {

    protected String inputDirName = "resources/compiled";
    protected String outputDirName = "resources/generated";

    protected File inputDir = null;
    protected File outputDir = null;

    protected AConstructorConverter constructorConverter;
    protected AFieldConverter fieldConverter;

    private JavaFieldReader fieldReader;

    public AClassConverter(String inputDir, String outputDir, AConstructorConverter constructorConverter, AFieldConverter fieldConverter, ADatatypeConverter datatypeConverter) {

        // input dir
        if (null != inputDir && !inputDir.isEmpty()) {
            this.inputDirName = inputDir;
        }
        this.inputDir = new File(this.inputDirName);
        if (!this.inputDir.exists() || !this.inputDir.isDirectory()) {
            throw new RuntimeException("Input Dir " + this.inputDir.getAbsolutePath() + " does not exists or it is not a directory. Otherwise check for permissions");
        }

        // output dir
        if (null != outputDir && !outputDir.isEmpty()) {
            this.outputDirName = outputDir;
        }
        this.outputDir = new File(this.outputDirName);
        if (!this.outputDir.exists() || !this.outputDir.isDirectory()) {
            throw new RuntimeException("Input Dir " + this.outputDir.getAbsolutePath() + " does not exists or it is not a directory. Otherwise check for permissions");
        }

        // field reader
        this.fieldReader = new JavaFieldReader(datatypeConverter);

        // sub converters
        this.constructorConverter = constructorConverter;
        this.fieldConverter = fieldConverter;
    }

    /**
     * load classes received as params and convert them with reflection
     * resulting files are written into outputDirName
     *
     * @param fqNameList list of full qualified names classes to convert
     */
    public void convertClassList(List<String> fqNameList) {

        if (null != fqNameList) {
            for (int i = 0; i < fqNameList.size(); i++) {
                JMCClass clz = this.loadClass(fqNameList.get(i));
                clz = this.fieldReader.readClassFields(clz);
                this.convertClass(clz);
            }
        }
    }

    /**
     * load one class from inputDirName and returns it
     *
     * @param className the class' full qualified name to load
     * @return loaded class wrapped into JMCClass
     */
    private JMCClass loadClass(String className) {

        try {
            // Convert File to a URL
            URL url = this.inputDir.toURI().toURL();
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader cl = new URLClassLoader(urls);

            return new JMCClass(cl.loadClass(className));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * converts received class into desired language
     *
     * @param clz class to convert
     */
    private void convertClass(@Nullable JMCClass clz) {

        if (null != clz) {

            // generate fields
            this.fieldConverter.convertFieldList(clz.getFieldList());

            // generate class name
            this.convertClassName(clz);

            // generate constructor
            this.constructorConverter.createConstructor(clz);

            // write class
            this.writeGeneratedClass(clz);
        }
    }


    /**
     * converts class name into desired language and set it into received JMCClass
     *
     * @param clz the JMCClass from which get name to convert
     * @return JMCClass with name converted into desired language
     */
    protected abstract JMCClass convertClassName(@Nullable JMCClass clz);



    /**
     * construct ang write class name into desired language
     *
     * @param clz the JMCClass of which generate code
     */
    protected abstract void writeGeneratedClass(@Nullable JMCClass clz);
}
