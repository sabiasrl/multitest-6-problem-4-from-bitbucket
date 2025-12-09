const {
    findAllStudents,
    findStudentDetail,
    addOrUpdateStudent,
    findStudentToSetStatus,
    getRoleId,
} = require("../students-repository");
const { processDBRequest } = require("../../../utils");

// Mock the utils module
jest.mock("../../../utils", () => ({
    processDBRequest: jest.fn(),
}));

describe("Students Repository", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe("getRoleId", () => {
        it("should return role id for given role name", async () => {
            const roleName = "Student";
            const mockResult = {
                rows: [{ id: 3 }],
            };
            processDBRequest.mockResolvedValue(mockResult);

            const result = await getRoleId(roleName);

            expect(processDBRequest).toHaveBeenCalledWith({
                query: "SELECT id FROM roles WHERE name ILIKE $1",
                queryParams: [roleName],
            });
            expect(result).toBe(3);
        });
    });

    describe("findAllStudents", () => {
        it("should return all students without filters", async () => {
            const mockPayload = {};
            const mockStudents = [
                { id: 1, name: "John Doe", email: "john@example.com" },
                { id: 2, name: "Jane Smith", email: "jane@example.com" },
            ];
            processDBRequest.mockResolvedValue({ rows: mockStudents });

            const result = await findAllStudents(mockPayload);

            expect(processDBRequest).toHaveBeenCalled();
            expect(result).toEqual(mockStudents);
        });

        it("should return filtered students by name", async () => {
            const mockPayload = { name: "John" };
            const mockStudents = [{ id: 1, name: "John Doe", email: "john@example.com" }];
            processDBRequest.mockResolvedValue({ rows: mockStudents });

            const result = await findAllStudents(mockPayload);

            expect(processDBRequest).toHaveBeenCalled();
            const callArgs = processDBRequest.mock.calls[0][0];
            expect(callArgs.query).toContain("t1.name = $1");
            expect(callArgs.queryParams).toContain("John");
            expect(result).toEqual(mockStudents);
        });

        it("should return filtered students by className, section, and roll", async () => {
            const mockPayload = {
                className: "10",
                section: "A",
                roll: "1",
            };
            const mockStudents = [{ id: 1, name: "John Doe" }];
            processDBRequest.mockResolvedValue({ rows: mockStudents });

            const result = await findAllStudents(mockPayload);

            expect(processDBRequest).toHaveBeenCalled();
            const callArgs = processDBRequest.mock.calls[0][0];
            expect(callArgs.query).toContain("class_name");
            expect(callArgs.query).toContain("section_name");
            expect(callArgs.query).toContain("roll");
            expect(callArgs.queryParams).toContain("10");
            expect(callArgs.queryParams).toContain("A");
            expect(callArgs.queryParams).toContain("1");
            expect(result).toEqual(mockStudents);
        });
    });

    describe("findStudentDetail", () => {
        it("should return student detail by id", async () => {
            const studentId = "1";
            const mockStudent = {
                id: 1,
                name: "John Doe",
                email: "john@example.com",
                class: "10",
                section: "A",
            };
            processDBRequest.mockResolvedValue({ rows: [mockStudent] });

            const result = await findStudentDetail(studentId);

            expect(processDBRequest).toHaveBeenCalledWith({
                query: expect.stringContaining("WHERE u.id = $1"),
                queryParams: [studentId],
            });
            expect(result).toEqual(mockStudent);
        });

        it("should return null when student not found", async () => {
            const studentId = "999";
            processDBRequest.mockResolvedValue({ rows: [] });

            const result = await findStudentDetail(studentId);

            expect(result).toBeUndefined();
        });
    });

    describe("addOrUpdateStudent", () => {
        it("should add or update student successfully", async () => {
            const mockPayload = {
                name: "John Doe",
                email: "john@example.com",
            };
            const mockResult = {
                rows: [
                    {
                        status: true,
                        userId: "1",
                        message: "Student added successfully",
                    },
                ],
            };
            processDBRequest.mockResolvedValue(mockResult);

            const result = await addOrUpdateStudent(mockPayload);

            expect(processDBRequest).toHaveBeenCalledWith({
                query: "SELECT * FROM student_add_update($1)",
                queryParams: [mockPayload],
            });
            expect(result).toEqual(mockResult.rows[0]);
        });
    });

    describe("findStudentToSetStatus", () => {
        it("should update student status successfully", async () => {
            const mockPayload = {
                userId: "1",
                reviewerId: "2",
                status: true,
            };
            processDBRequest.mockResolvedValue({ rowCount: 1 });

            const result = await findStudentToSetStatus(mockPayload);

            expect(processDBRequest).toHaveBeenCalled();
            const callArgs = processDBRequest.mock.calls[0][0];
            expect(callArgs.query).toContain("UPDATE users");
            expect(callArgs.query).toContain("is_active = $1");
            expect(callArgs.queryParams[0]).toBe(true);
            expect(callArgs.queryParams[3]).toBe("1");
            expect(result).toBe(1);
        });

        it("should return 0 when no rows affected", async () => {
            const mockPayload = {
                userId: "999",
                reviewerId: "2",
                status: false,
            };
            processDBRequest.mockResolvedValue({ rowCount: 0 });

            const result = await findStudentToSetStatus(mockPayload);

            expect(result).toBe(0);
        });
    });
});

