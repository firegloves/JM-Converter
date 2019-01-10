package it.caneserpente.javamodelconverter.converter.typescript;

import com.sun.istack.internal.Nullable;
import it.caneserpente.javamodelconverter.converter.base.AConstructorConverter;
import it.caneserpente.javamodelconverter.converter.base.ADatatypeConverter;
import it.caneserpente.javamodelconverter.model.*;

import java.util.Arrays;
import java.util.List;

public class TypescriptConstructorConverter extends AConstructorConverter {

    private final List<String> NO_CONSTRUCTOR_DATA_TYPES = Arrays.asList("number", "string", "String", "boolean", "Boolean");

    /**
     * constructor
     */
    public TypescriptConstructorConverter(ADatatypeConverter datatypeConverter) {
        super(datatypeConverter);
        this.datatypeConverter = datatypeConverter;
    }


    @Override
    public JMCClass createConstructor(@Nullable JMCClass clz) {

        if (null != clz) {
            clz.setConvertedConstructorInit("\tconstructor()\n" +
                    "\tconstructor(m: " + clz.getConvertedClassName() + ")\n" +
                    "\tconstructor(m?: " + clz.getConvertedClassName() + ") {\n");

            clz.getFieldList().stream().forEach(f -> this.createConstructorFieldAssignment(f));
        }

        return clz;
    }


    @Override
    protected String createConstrJMCFieldBasic(JMCFieldBasic jf) {

        switch (jf.getJavaTypeName()) {

            case "java.util.Date":
            case "java.sql.Timestamp":
                return "\t\tthis." + jf.getJavaField().getName() + " = m && m." + jf.getJavaField().getName() + " ? new Date(m." + jf.getJavaField().getName() + ") : undefined;\n";

            default:
                return "\t\tthis." + jf.getJavaField().getName() + " = m && m." + jf.getJavaField().getName() + " || undefined;\n";
        }
    }

    @Override
    protected String createConstrJMCFieldArray(JMCFieldArray jf) {
        return this.createConstrJMCFieldArrayOrCollection(jf);
    }

    @Override
    protected String createConstrJMCFieldCollection(JMCFieldCollection jf) {
        return this.createConstrJMCFieldArrayOrCollection(jf);
    }

    @Override
    protected String createConstrJMCFieldMap(JMCFieldMap jf) {

        String converted = "\t\tthis." + jf.getJavaField().getName() + " = ";
        String convertedKey;
        String convertedValue;

        if (! jf.isParametrized()) {
            converted += "m && m." + jf.getJavaField().getName() + ";\n";
        } else {
            converted += "new Map<" + jf.getConvertedFieldKeyType() + ", " + jf.getConvertedFieldValueType() + ">();\n";
            converted += "\t\tArray.from(m." + jf.getJavaField().getName() + ".keys()).forEach(k => {\n";

            if (! NO_CONSTRUCTOR_DATA_TYPES.contains(jf.getConvertedFieldKeyType())) {
                convertedKey = "new " + jf.getConvertedFieldKeyType() + "(k)";
            } else {
                convertedKey = "k";
            }
            if (! NO_CONSTRUCTOR_DATA_TYPES.contains(jf.getConvertedFieldValueType())) {
                convertedValue = "new " + jf.getConvertedFieldValueType() + "(m.get(k))";
            } else {
                convertedValue = "m.get(k)";
            }

            converted += "\t\t\tm.set(" + convertedKey + ", " + convertedValue + ");\n";
            converted += "\t\t};\n";
        }

        return converted;
    }



    /**
     * converts JMCFieldWithSubtype
     *
     * @param jf the JMCFieldWithSubtype to convert
     * @return JMCField converted
     */
    private String createConstrJMCFieldArrayOrCollection(JMCFieldWithSubtype jf) {
        if (null == jf.getConvertedSubtype() || jf.getConvertedSubtype().isEmpty() || NO_CONSTRUCTOR_DATA_TYPES.contains(jf.getConvertedSubtype())) {
            return "\t\tthis." + jf.getJavaField().getName() + " = m && m." + jf.getJavaField().getName() + ";\n";
        } else {
            return "\t\tthis." + jf.getJavaField().getName() + " = m && m." + jf.getJavaField().getName() + " ? m." + jf.getJavaField().getName() + ".map(s => new " + jf.getConvertedSubtype() + "(s)) : [];\n";
        }
    }
}
