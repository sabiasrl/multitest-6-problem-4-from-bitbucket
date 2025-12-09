const { z } = require("zod");
const {
    createStudentSchema,
    updateStudentSchema,
    getAllStudentsSchema,
    getStudentDetailSchema,
    studentStatusSchema,
} = require("../students-validator");

describe("Students Validator", () => {
    describe("createStudentSchema", () => {
        it("should validate correct student creation data", () => {
            const validData = {
                body: {
                    name: "John Doe",
                    email: "john@example.com",
                    phone: "1234567890",
                    gender: "male",
                    dob: "2010-01-01",
                    class: "10",
                    section: "A",
                    roll: "1",
                    fatherName: "Father Name",
                    fatherPhone: "1234567890",
                    motherName: "Mother Name",
                    motherPhone: "0987654321",
                    guardianName: "Guardian Name",
                    guardianPhone: "1122334455",
                    relationOfGuardian: "Uncle",
                    currentAddress: "123 Main St",
                    permanentAddress: "123 Main St",
                    admissionDate: "2020-01-01",
                    systemAccess: true,
                },
            };

            expect(() => createStudentSchema.parse(validData)).not.toThrow();
        });

        it("should reject missing required fields", () => {
            const invalidData = {
                body: {
                    email: "john@example.com",
                    // Missing name
                },
            };

            expect(() => createStudentSchema.parse(invalidData)).toThrow();
        });

        it("should reject invalid email format", () => {
            const invalidData = {
                body: {
                    name: "John Doe",
                    email: "invalid-email",
                    phone: "1234567890",
                    gender: "male",
                    dob: "2010-01-01",
                    class: "10",
                    section: "A",
                    roll: "1",
                    fatherName: "Father Name",
                    guardianName: "Guardian Name",
                    guardianPhone: "1122334455",
                    relationOfGuardian: "Uncle",
                    currentAddress: "123 Main St",
                    permanentAddress: "123 Main St",
                    admissionDate: "2020-01-01",
                },
            };

            expect(() => createStudentSchema.parse(invalidData)).toThrow();
        });

        it("should reject invalid date format", () => {
            const invalidData = {
                body: {
                    name: "John Doe",
                    email: "john@example.com",
                    phone: "1234567890",
                    gender: "male",
                    dob: "01-01-2010", // Invalid format
                    class: "10",
                    section: "A",
                    roll: "1",
                    fatherName: "Father Name",
                    guardianName: "Guardian Name",
                    guardianPhone: "1122334455",
                    relationOfGuardian: "Uncle",
                    currentAddress: "123 Main St",
                    permanentAddress: "123 Main St",
                    admissionDate: "2020-01-01",
                },
            };

            expect(() => createStudentSchema.parse(invalidData)).toThrow();
        });
    });

    describe("updateStudentSchema", () => {
        it("should validate correct student update data", () => {
            const validData = {
                body: {
                    name: "John Updated",
                },
                params: {
                    id: "1",
                },
            };

            expect(() => updateStudentSchema.parse(validData)).not.toThrow();
        });

        it("should reject empty update body", () => {
            const invalidData = {
                body: {},
                params: {
                    id: "1",
                },
            };

            expect(() => updateStudentSchema.parse(invalidData)).toThrow("At least one field must be provided for update");
        });

        it("should reject missing student id in params", () => {
            const invalidData = {
                body: {
                    name: "John Updated",
                },
                params: {},
            };

            expect(() => updateStudentSchema.parse(invalidData)).toThrow();
        });

        it("should validate partial update with multiple fields", () => {
            const validData = {
                body: {
                    name: "John Updated",
                    email: "john.updated@example.com",
                    phone: "9876543210",
                },
                params: {
                    id: "1",
                },
            };

            expect(() => updateStudentSchema.parse(validData)).not.toThrow();
        });
    });

    describe("getAllStudentsSchema", () => {
        it("should validate query parameters", () => {
            const validData = {
                query: {
                    name: "John",
                    className: "10",
                    section: "A",
                    roll: "1",
                },
            };

            expect(() => getAllStudentsSchema.parse(validData)).not.toThrow();
        });

        it("should validate empty query parameters", () => {
            const validData = {
                query: {},
            };

            expect(() => getAllStudentsSchema.parse(validData)).not.toThrow();
        });

        it("should validate partial query parameters", () => {
            const validData = {
                query: {
                    name: "John",
                },
            };

            expect(() => getAllStudentsSchema.parse(validData)).not.toThrow();
        });
    });

    describe("getStudentDetailSchema", () => {
        it("should validate student id parameter", () => {
            const validData = {
                params: {
                    id: "1",
                },
            };

            expect(() => getStudentDetailSchema.parse(validData)).not.toThrow();
        });

        it("should reject missing student id", () => {
            const invalidData = {
                params: {},
            };

            expect(() => getStudentDetailSchema.parse(invalidData)).toThrow();
        });

        it("should reject empty student id", () => {
            const invalidData = {
                params: {
                    id: "",
                },
            };

            expect(() => getStudentDetailSchema.parse(invalidData)).toThrow();
        });
    });

    describe("studentStatusSchema", () => {
        it("should validate status update with boolean true", () => {
            const validData = {
                body: {
                    status: true,
                },
                params: {
                    id: "1",
                },
            };

            expect(() => studentStatusSchema.parse(validData)).not.toThrow();
        });

        it("should validate status update with boolean false", () => {
            const validData = {
                body: {
                    status: false,
                },
                params: {
                    id: "1",
                },
            };

            expect(() => studentStatusSchema.parse(validData)).not.toThrow();
        });

        it("should reject missing status", () => {
            const invalidData = {
                body: {},
                params: {
                    id: "1",
                },
            };

            expect(() => studentStatusSchema.parse(invalidData)).toThrow();
        });

        it("should reject non-boolean status", () => {
            const invalidData = {
                body: {
                    status: "active",
                },
                params: {
                    id: "1",
                },
            };

            expect(() => studentStatusSchema.parse(invalidData)).toThrow();
        });

        it("should reject missing student id", () => {
            const invalidData = {
                body: {
                    status: true,
                },
                params: {},
            };

            expect(() => studentStatusSchema.parse(invalidData)).toThrow();
        });
    });
});

