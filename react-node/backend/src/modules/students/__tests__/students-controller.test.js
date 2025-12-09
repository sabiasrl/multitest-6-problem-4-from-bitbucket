const {
    handleGetAllStudents,
    handleAddStudent,
    handleUpdateStudent,
    handleGetStudentDetail,
    handleStudentStatus,
} = require("../students-controller");
const { getAllStudents, addNewStudent, getStudentDetail, setStudentStatus, updateStudent } = require("../students-service");

// Mock the service module
jest.mock("../students-service");

describe("Students Controller", () => {
    let req, res, next;

    beforeEach(() => {
        req = {
            query: {},
            body: {},
            params: {},
            user: {},
        };
        res = {
            json: jest.fn(),
            status: jest.fn().mockReturnThis(),
        };
        next = jest.fn();
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    describe("handleGetAllStudents", () => {
        it("should return all students with query filters", async () => {
            const mockStudents = [
                { id: 1, name: "John Doe", email: "john@example.com" },
                { id: 2, name: "Jane Smith", email: "jane@example.com" },
            ];
            req.query = { name: "John", className: "10", section: "A", roll: "1" };
            getAllStudents.mockResolvedValue(mockStudents);

            await handleGetAllStudents(req, res, next);

            expect(getAllStudents).toHaveBeenCalledWith({
                name: "John",
                className: "10",
                section: "A",
                roll: "1",
            });
            expect(res.json).toHaveBeenCalledWith({ students: mockStudents });
        });

        it("should handle empty query parameters", async () => {
            const mockStudents = [{ id: 1, name: "John Doe" }];
            req.query = {};
            getAllStudents.mockResolvedValue(mockStudents);

            await handleGetAllStudents(req, res, next);

            expect(getAllStudents).toHaveBeenCalledWith({
                name: undefined,
                className: undefined,
                section: undefined,
                roll: undefined,
            });
            expect(res.json).toHaveBeenCalledWith({ students: mockStudents });
        });
    });

    describe("handleAddStudent", () => {
        it("should add a new student successfully", async () => {
            const mockPayload = {
                name: "John Doe",
                email: "john@example.com",
                phone: "1234567890",
            };
            const mockResponse = { message: "Student added and verification email sent successfully." };
            req.body = mockPayload;
            addNewStudent.mockResolvedValue(mockResponse);

            await handleAddStudent(req, res, next);

            expect(addNewStudent).toHaveBeenCalledWith(mockPayload);
            expect(res.json).toHaveBeenCalledWith(mockResponse);
        });
    });

    describe("handleUpdateStudent", () => {
        it("should update a student successfully", async () => {
            const mockPayload = { name: "John Updated", email: "john.updated@example.com" };
            const mockResponse = { message: "Student updated successfully" };
            req.params = { id: "1" };
            req.body = mockPayload;
            updateStudent.mockResolvedValue(mockResponse);

            await handleUpdateStudent(req, res, next);

            expect(updateStudent).toHaveBeenCalledWith({
                ...mockPayload,
                userId: "1",
            });
            expect(res.json).toHaveBeenCalledWith(mockResponse);
        });
    });

    describe("handleGetStudentDetail", () => {
        it("should return student detail by id", async () => {
            const mockStudent = {
                id: 1,
                name: "John Doe",
                email: "john@example.com",
                class: "10",
                section: "A",
            };
            req.params = { id: "1" };
            getStudentDetail.mockResolvedValue(mockStudent);

            await handleGetStudentDetail(req, res, next);

            expect(getStudentDetail).toHaveBeenCalledWith("1");
            expect(res.json).toHaveBeenCalledWith(mockStudent);
        });
    });

    describe("handleStudentStatus", () => {
        it("should update student status successfully", async () => {
            const mockPayload = { status: true };
            const mockResponse = { message: "Student status changed successfully" };
            req.params = { id: "1" };
            req.body = mockPayload;
            req.user = { id: "2" };
            setStudentStatus.mockResolvedValue(mockResponse);

            await handleStudentStatus(req, res, next);

            expect(setStudentStatus).toHaveBeenCalledWith({
                ...mockPayload,
                userId: "1",
                reviewerId: "2",
            });
            expect(res.json).toHaveBeenCalledWith(mockResponse);
        });
    });
});

