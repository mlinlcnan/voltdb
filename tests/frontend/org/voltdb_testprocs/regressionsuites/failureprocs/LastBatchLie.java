/* This file is part of VoltDB.
 * Copyright (C) 2008-2012 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb_testprocs.regressionsuites.failureprocs;

import org.voltdb.ProcInfo;
import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

@ProcInfo (
    singlePartition = false
)
public class LastBatchLie extends VoltProcedure {
    public final SQLStmt insert = new SQLStmt("INSERT INTO NEW_ORDER VALUES (?, ?, ?);");

    public VoltTable[] run() {
        // Do some multi-batch just to show it's not broken
        for (int j = 0; j < 10; j++) {
            voltQueueSQL(insert, j, j, j);
            voltExecuteSQL(false);
        }
        voltQueueSQL(insert, 10, 10, 10);
        voltExecuteSQL(true);
        voltQueueSQL(insert, 11, 11, 11);
        // This should cause a VoltAbortException
        voltExecuteSQL(true);
        return null;
    }
}
