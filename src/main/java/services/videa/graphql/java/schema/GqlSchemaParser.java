/*
 * Copyright 2019 Videa Project Services GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package services.videa.graphql.java.schema;

import graphql.language.*;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * GraphQL Schema Parser containing Java object of all schema types.
 */
public class GqlSchemaParser {
    private static Logger logger = LoggerFactory.getLogger(GqlSchemaParser.class);

    private TypeDefinitionRegistry typeRegistry;

    private Map<String, ObjectTypeDefinition> objectTypes;
    private Map<String, InputObjectTypeDefinition> inputTypes;
    private Map<String, EnumTypeDefinition> enums;
    private Map<String, InterfaceTypeDefinition> interfaces;

    public GqlSchemaParser(File gqlSchema) {
        logger.debug("gqlSchema: {}", gqlSchema);

        SchemaParser schemaParser = new SchemaParser();
        typeRegistry = schemaParser.parse(gqlSchema);
        logger.debug("typeRegistry: {}", typeRegistry);
    }

    public GqlSchemaParser(String schemaContent) {
        if(schemaContent == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " schemaContent is null but must be defined");
        }
        logger.debug("schemaContent: {}", schemaContent);

        SchemaParser schemaParser = new SchemaParser();
        typeRegistry = schemaParser.parse(schemaContent);

        logger.debug("typeRegistry: {}", typeRegistry);
    }

    /**
     * Search for a type definition with given name and return if existent.
     * Return null otherwise.
     *
     * @param name GraphQL type to look for
     * @return TypeDefinition derived from type name.
     */
    public TypeDefinition findByName(String name) {
        if(this.scalars().containsKey(name)) {
            return this.scalars().get(name);
        }
        if(this.objectTypes().containsKey(name)) {
            return this.objectTypes().get(name);
        }
        if(this.inputTypes().containsKey(name)) {
            return this.inputTypes().get(name);
        }
        if(this.enums().containsKey(name)) {
            return this.enums().get(name);
        }
        if(this.interfaces().containsKey(name)) {
            return this.interfaces().get(name);
        }
        return null;
    }

    public Map<String, ScalarTypeDefinition> scalars() {
        return typeRegistry.scalars();
    }

    /**
     * Return both object and input types.
     *
     * @return Java types derived from schema.
     */
    public Map<String, TypeDefinition> types() {
        return typeRegistry.types();
    }

    public Map<String, ObjectTypeDefinition> objectTypes() {
        if (this.objectTypes == null) {
            this.objectTypes = new HashMap<>();

            this.typeRegistry.types().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof ObjectTypeDefinition) {
                    this.objectTypes.put(entry.getKey(), (ObjectTypeDefinition) entry.getValue());
                }
            });
        }
        return this.objectTypes;
    }


    /**
     * Return all input types
     *
     * @return Input types from schema
     */
    public Map<String, InputObjectTypeDefinition> inputTypes() {
        if (this.inputTypes == null) {
            this.inputTypes = new HashMap<>();

            this.typeRegistry.types().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof InputObjectTypeDefinition) {
                    this.inputTypes.put(entry.getKey(), (InputObjectTypeDefinition) entry.getValue());
                }
            });
        }
        return this.inputTypes;
    }


    /**
     * Return all enum types
     *
     * @return Enum types derived from schema.
     */
    public Map<String, EnumTypeDefinition> enums() {
        if (this.enums == null) {
            this.enums = new HashMap<>();

            this.typeRegistry.types().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof EnumTypeDefinition) {
                    this.enums.put(entry.getKey(), (EnumTypeDefinition) entry.getValue());
                }
            });
        }
        return this.enums;
    }


    /**
     * Return all interfaces
     *
     * @return Interfaces derived from schema.
     */
    public Map<String, InterfaceTypeDefinition> interfaces() {
        if (this.interfaces == null) {
            this.interfaces = new HashMap<>();

            this.typeRegistry.types().entrySet().forEach(entry -> {
                if (entry.getValue() instanceof InterfaceTypeDefinition) {
                    this.interfaces.put(entry.getKey(), (InterfaceTypeDefinition) entry.getValue());
                }
            });
        }
        return this.interfaces;
    }

}
