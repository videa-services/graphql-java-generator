/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package services.videa.graphql.java.pojos;

import org.junit.Before;
import org.junit.Test;

public class JavaModelGeneratorTest {

    private JavaModelGenerator javaModelGenerator;

    @Before
    public void setUp() {
        GraphQLSchemaParser graphQLSchemaParser = new GraphQLSchemaParser("/zemtu.gql");
        javaModelGenerator = new JavaModelGenerator(
                graphQLSchemaParser, "de.teilautos.zemtu.graphql.types");
    }

    @Test
    public void generateAll() {
        javaModelGenerator.toModels();
    }

    @Test
    public void generateErrorType() {
        javaModelGenerator.toModel("ErrorType");
    }

    @Test
    public void generateUserNodeEdge() {
        javaModelGenerator.toModel("UserNodeEdge");
    }

    @Test
    public void generateUserNodeConnection() {
        javaModelGenerator.toModel("UserNodeConnection");
    }

    @Test
    public void generateUserNodeEdgeConnection() {
        javaModelGenerator.toModel("UserNode");
        javaModelGenerator.toModel("UserNodeEdge");
        javaModelGenerator.toModel("UserNodeConnection");
    }

    @Test
    public void generateAccessReservationInput() {
        javaModelGenerator.toModel("AccessReservationInput");
    }

    @Test
    public void generateAccessReservationPayload() {
        javaModelGenerator.toModel("AccessReservationPayload");
    }

    @Test
    public void generateApproveMembershipInput() {
        javaModelGenerator.toModel("ApproveMembershipInput");
    }

    @Test
    public void generateReservationActionTypeEnum() {
        javaModelGenerator.toModel("ReservationActionTypeEnum");
    }

    @Test
    public void generateDataItem() {
        javaModelGenerator.toModel("DataItem");
    }

    @Test
    public void generateQuery() {
        javaModelGenerator.toEntryPoint("Query");
    }

    @Test
    public void generateMutation() {
        javaModelGenerator.toEntryPoint("Mutation");
    }
}