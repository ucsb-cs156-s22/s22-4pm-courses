import React from "react";
import OurTable from "main/components/OurTable";

//import { yyyyqToQyy } from "main/utils/quarterUtilities.js";

export default function BasicCourseTable({ courses }) {

    const columns = [
        {
            Header: 'id',
            accessor: 'id',
        },
        {
            Header: 'enrollCd',
            accessor: 'enrollCd',
        },
        {
            Header: 'psId',
            accessor: 'psId',
        },
    ];

    return <OurTable
        data={courses}
        columns={columns}
        testid={"BasicCourseTable"}
    />;
};