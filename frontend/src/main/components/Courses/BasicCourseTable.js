import React from "react";
import OurTable from "main/components/OurTable";

//import { yyyyqToQyy } from "main/utils/quarterUtilities.js";

export default function BasicCourseTable({ courses }) {

    const columns = [
        {
            Header: 'Id',
            accessor: 'id',
        },
        {
            Header: 'Enrollment code',
            accessor: 'enrollCd',
        },
        {
            Header: 'Personal Schedule Id',
            accessor: 'psId',
        }
    ];

    return <OurTable
        data={courses}
        columns={columns}
        testid={"BasicCourseTable"}
    />;
};