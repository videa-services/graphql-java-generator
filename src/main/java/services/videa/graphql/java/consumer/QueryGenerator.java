package services.videa.graphql.java.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.*;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryGenerator extends AbstractGenerator {
    private static Logger logger = LoggerFactory.getLogger(QueryGenerator.class);

    private ObjectTypeDefinition queryTypeDefinition;


    public QueryGenerator(ObjectTypeDefinition queryTypeDefinition, String targetPackageName) {
        logger.debug("queryTypeDefinition={}, targetPackageName={}", queryTypeDefinition, targetPackageName);

        this.queryTypeDefinition = queryTypeDefinition;
        this.targetPackageName = targetPackageName;
    }


    public void createClass() {
        logger.trace("{}", "entering");

        List<MethodSpec> methodSpecList = queryTypeDefinition.getFieldDefinitions().stream().map(methodDefinition -> {

            String methodName = methodDefinition.getName();
            logger.debug("methodName={}", methodName);

            String returnTypeName = ((TypeName) methodDefinition.getType()).getName();
            Class<?> returnTypeClass = toTypeClazz(returnTypeName, targetPackageName);
            logger.debug("returnTypeName={}, returnTypeClass={}", returnTypeName, returnTypeClass);

            List<ParameterSpec> parameterSpecList =
                    methodDefinition.getInputValueDefinitions().stream().map(parameterDefinition -> {
                        String typeName = extractTypeName(parameterDefinition.getType());
                        Class<?> parameterClazz = toTypeClazz(typeName, targetPackageName);
                        return ParameterSpec.builder(parameterClazz, parameterDefinition.getName()).build();
                    }).collect(Collectors.toList());

            String methodBody = generateMethodBody(methodName, returnTypeName, parameterSpecList);

            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addException(IOException.class)
                    .returns(returnTypeClass)
                    .addParameters(parameterSpecList)
                    .addCode(CodeBlock.of(methodBody) + NEWLINE)
                    .build();

        }).collect(Collectors.toList());

        String comment = extractComment(queryTypeDefinition.getDescription());

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(FieldSpec.builder(RestTemplate.class, "restTemplate", Modifier.PRIVATE).build());
        fieldSpecs.add(FieldSpec.builder(DefaultUriBuilderFactory.class, "defaultUriBuilderFactory",
                Modifier.PRIVATE).build());
        fieldSpecs.add(FieldSpec.builder(ObjectMapper.class, "objectMapper", Modifier.PRIVATE).build());

        MethodSpec constructorMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "url")
                .addParameter(String.class, "token")
                .addStatement("this.defaultUriBuilderFactory = new DefaultUriBuilderFactory(url)")
                .addStatement("this.restTemplate = init(token)")
                .addStatement("this.objectMapper = new ObjectMapper()")
                .build();

        MethodSpec initMethodSpec = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(String.class, "token")
                .addCode(CodeBlock.of("            RestTemplate restTemplate = new RestTemplate();\n" +
                        "            restTemplate.setUriTemplateHandler(this.defaultUriBuilderFactory);\n" +
                        "            restTemplate.getInterceptors().add((request, body, execution) -> {\n" +
                        "                request.getHeaders().set(\"Authorization\", \"Token \" + token);\n" +
                        "                return execution.execute(request, body);\n" +
                        "            });\n" +
                        "            return restTemplate;\n"))
                .returns(RestTemplate.class)
                .build();

        MethodSpec readFieldsMethodSpec = MethodSpec.methodBuilder("readFields")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Class.class, "aClass")
                .addCode(CodeBlock.of(generateReadFieldsBody()))
                .returns(String.class)
                .build();

        TypeSpec requestTypeSpec = TypeSpec.classBuilder("Request")
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(Getter.class).addAnnotation(Setter.class)
                .addField(String.class, "query", Modifier.PRIVATE)
                .addField(Map.class, "variables", Modifier.PRIVATE)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(queryTypeDefinition.getName())
                .addJavadoc(comment)
                .addAnnotation(Data.class)
                .addModifiers(Modifier.PUBLIC)
                .addType(requestTypeSpec)
                .addFields(fieldSpecs)
                .addMethod(constructorMethodSpec)
                .addMethod(initMethodSpec)
                .addMethod(readFieldsMethodSpec)
                .addMethods(methodSpecList)
                .build();

        writeClass(typeSpec);

        logger.trace("{}", "exiting");
    }

    private String generateMethodBody(String methodName, String returnTypeName, List<ParameterSpec> parameterSpecList) {
        StringBuffer body = new StringBuffer();
        body.append("Request request = new Request();").append(NEWLINE);

        body.append("String jsonBody = \"query { ").append(methodName).append( " \";").append(NEWLINE);

        body.append("jsonBody += \" ( \";").append(NEWLINE);
        body.append("int parameterStrSize = jsonBody.length();").append(NEWLINE);
        String parameterList = generateParameterList(parameterSpecList);

        body.append(parameterList);
        body.append("if(parameterStrSize < jsonBody.length()) {").append(NEWLINE);
        body.append("    jsonBody = jsonBody.substring(0, jsonBody.length() - 2);").append(NEWLINE);
        body.append("}").append(NEWLINE);
        body.append("jsonBody += \" ) \";").append(NEWLINE);

        body.append("jsonBody += \" { \";").append(NEWLINE);
        body.append("jsonBody += readFields(" + returnTypeName + ".class);").append(NEWLINE);
        body.append("jsonBody += \" } \";").append(NEWLINE);

        body.append("jsonBody += \" } \";").append(NEWLINE);

        body.append("request.setQuery(jsonBody);").append(NEWLINE);
        body.append("request.setVariables(java.util.Collections.emptyMap());").append(NEWLINE);

        body.append("Object response = restTemplate.postForObject(\"\", request, Object.class);").append(NEWLINE);
        body.append(
                "java.util.Map map = objectMapper.convertValue(response, java.util.Map.class);")
                .append(NEWLINE);
        body.append("Map nodeMap = (Map)((Map)map.get(\"data\")).get(\"" + methodName + "\");").append(NEWLINE);

        body.append("String responseJson = objectMapper.writeValueAsString(nodeMap);").append(NEWLINE);
        body.append(
                returnTypeName + " nodeObject = objectMapper.readValue(responseJson, " + returnTypeName + ".class);")
                .append(NEWLINE);

        body.append("return nodeObject;").append(NEWLINE);

        return body.toString();
    }

    private String generateParameterList(List<ParameterSpec> parameterSpecList) {
        StringBuffer generationBuffer = new StringBuffer();
        parameterSpecList.stream().forEach(parameterSpec -> {
            String format = MessageFormat.format("if({0} != null) ", parameterSpec.name) + " { ";
            generationBuffer.append(format).append(NEWLINE);

            String type = "String".equals(((ClassName)parameterSpec.type).simpleName()) ? "\\\"{0}\\\", \"" : "{0}, \"";
            String pair = "    jsonBody += java.text.MessageFormat.format(\""
                    + parameterSpec.name + ":" + type + ", " + parameterSpec.name+ ");";
            generationBuffer.append(pair).append(NEWLINE);

            generationBuffer.append("}").append(NEWLINE);
        });

        return generationBuffer.toString();
    }

    private String generateReadFieldsBody() {
        String impl =
                "        StringBuffer buffer = new StringBuffer();\n" +
                "        java.lang.reflect.Field[] fields = aClass.getDeclaredFields();\n" +
                "        for (java.lang.reflect.Field field : fields) {\n" +
                "            buffer.append(field.getName()).append(\" \");\n" +
                "            if (!field.getType().getName().startsWith(\"java.lang\")) {\n" +
                "                buffer.append(\" { \").append(System.getProperty(\"line.separator\"));\n" +
                "                Class<?> myClass = null;\n" +
                "                try {\n" +
                "                    if (\"java.util.List\".equals(field.getType().getName())) {\n" +
                "                        java.lang.reflect.ParameterizedType typeName \n" +
                "                                = (java.lang.reflect.ParameterizedType) field.getGenericType();\n" +
                "                        myClass = Class.forName(typeName.getActualTypeArguments()[0].getTypeName());\n" +
                "                    } else {\n" +
                "                        myClass = field.getType();\n" +
                "                    }\n" +
                "                } catch(ClassNotFoundException e) {\n" +
                "                    e.printStackTrace(System.err);\n" +
                "                    System.err.println(\"Setting aClass to String.class\");\n" +
                "                    myClass = String.class;\n" +
                "                }\n" +
                "                String fieldsStr = readFields(myClass);\n" +
                "                buffer.append(fieldsStr).append(System.getProperty(\"line.separator\"));\n" +
                "                buffer.append(\"}\").append(System.getProperty(\"line.separator\"));\n" +
                "            }\n" +
                "        }\n" +
                "        return buffer.toString();\n";
        return impl;
    }
}