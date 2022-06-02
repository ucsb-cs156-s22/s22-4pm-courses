import {convertToFraction, formatLocation, isSectionAsString, formatDays, formatTime, formatInstructors, sectionNumber} from "main/utils/sectionUtils"

const testTimeLocations = [
    {
      "room": "1",
      "building": "LOC1",
      "roomCapacity": "90",
      "days": "M W",
      "beginTime": "15:30",
      "endTime": "16:45"
    },
    {
        "room": "2",
        "building": "LOC2",
        "roomCapacity": "90",
        "days": "R F",
        "beginTime": "10:30",
        "endTime": "11:45"
    }
]

const testTimeLocations1 = [
    {
      "room": "1",
      "building": "LOC1",
      "roomCapacity": "90",
      "days": null,
      "beginTime": "15:30",
      "endTime": "16:45"
    },
    {
        "room": "2",
        "building": "LOC2",
        "roomCapacity": "90",
        "days": "R F",
        "beginTime": "10:30",
        "endTime": "11:45"
    }
]

const testInstructors = [
    {
      "instructor": "HESPANHA J P",
      "functionCode": "Teaching and in charge"
    },
    {
        "instructor": "JOHN S",
        "functionCode": "Teaching and in charge"
    }
]

describe ("section utils tests", () => {
    test("convertToFraction one null test 1" , () => {
        expect(convertToFraction(null, "100")).toBe("");
    }); 

    test("convertToFraction one null test 2" , () => {
        expect(convertToFraction("100", null)).toBe("");
    }); 

    test("isSectionAsString yes test" , () => {
        expect(isSectionAsString("0104")).toBe("Yes");
    }); 

    test("formatLocation test" , () => {
        expect(formatLocation(testTimeLocations)).toBe("LOC1 1, LOC2 2");
    }); 

    test("formatDays test 1" , () => {
        expect(formatDays(testTimeLocations)).toBe("M W, R F");
    });
    
    test("formatDays test 2" , () => {
        expect(formatDays(testTimeLocations1)).toBe("R F");
    });

    test("formatTime test 3" , () => {
        expect(formatTime(testTimeLocations)).toBe("3:30 PM - 4:45 PM, 10:30 AM - 11:45 AM");
    }); 

    test("formatInstructors test" , () => {
        expect(formatInstructors(testInstructors)).toBe("HESPANHA J P, JOHN S");
    }); 

    test("sectionNumber test 1" , () => {
        expect(sectionNumber("0101")).toBe("0101");
    })

    test("sectionNumber test 2" , () => {
        expect(sectionNumber(null)).toBe("N/A");
    })

})