import {  render, screen } from "@testing-library/react";
import { fiveSections } from "fixtures/sectionFixtures";
import { QueryClient, QueryClientProvider } from "react-query";
import { MemoryRouter } from "react-router-dom";
import SectionsTable from "main/components/Sections/SectionsTable";


const mockedNavigate = jest.fn();

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockedNavigate
}));

describe("Section tests", () => {
  const queryClient = new QueryClient();


  test("renders without crashing for empty table", () => {

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SectionsTable sections={[]} />
        </MemoryRouter>
      </QueryClientProvider>

    );
  });



  test("Has the expected column headers and content", () => {

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SectionsTable sections={fiveSections} />
        </MemoryRouter>
      </QueryClientProvider>

    );


    const expectedHeaders = ["Quarter",  "Course ID", "Title", "Is Section?", "Section Number", "Enrolled", "Location", "Days", "Time", "Instructor", "Enroll Code"];
    const expectedFields = ["quarter", "courseInfo.courseId", "courseInfo.title", "isSection", "sectionNumber", "enrolled", "location", "days", "time", "instructor", "section.enrollCode"];
    const testId = "SectionsTable";

    expectedHeaders.forEach((headerText) => {
      const header = screen.getByText(headerText);
      expect(header).toBeInTheDocument();
    });

    expectedFields.forEach((field) => {
      const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
      expect(header).toBeInTheDocument();
    });

    expect(screen.getByTestId(`${testId}-cell-row-0-col-quarter`)).toHaveTextContent("W22");
    expect(screen.getByTestId(`${testId}-cell-row-0-col-time`)).toHaveTextContent("3:00 PM - 3:50 PM");
    expect(screen.getByTestId(`${testId}-cell-row-0-col-days`)).toHaveTextContent("M");
    expect(screen.getByTestId(`${testId}-cell-row-1-col-enrolled`)).toHaveTextContent("84/80");
    expect(screen.getByTestId(`${testId}-cell-row-1-col-isSection`)).toHaveTextContent("No");
    expect(screen.getByTestId(`${testId}-cell-row-2-col-location`)).toHaveTextContent("HFH 1124");
    expect(screen.getByTestId(`${testId}-cell-row-2-col-instructor`)).toHaveTextContent("YUNG A S");
    expect(screen.getByTestId(`${testId}-cell-row-2-col-instructor`)).toHaveTextContent("YUNG A S");
    expect(screen.getByTestId(`${testId}-cell-row-2-col-sectionNumber`)).toHaveTextContent("0101");


  });


});

