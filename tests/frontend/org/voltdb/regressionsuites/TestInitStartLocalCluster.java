/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
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

package org.voltdb.regressionsuites;


import org.voltdb.BackendTarget;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.utils.MiscUtils;

import junit.framework.Test;
import org.voltdb.benchmark.tpcc.TPCCProjectBuilder;


/**
 * Tests a mix of multi-partition and single partition procedures on a
 * mix of replicated and partititioned tables on a mix of single-site and
 * multi-site VoltDB instances.
 *
 */
public class TestInitStartLocalCluster extends RegressionSuite {

    static final int SITES_PER_HOST = 8;
    static final int HOSTS = 3;
    static final int K = MiscUtils.isPro() ? 1 : 0;
    // procedures used by these tests
    static Class<?>[] BASEPROCS = { org.voltdb.benchmark.tpcc.procedures.InsertNewOrder.class,
                                    org.voltdb.benchmark.tpcc.procedures.SelectAll.class,
                                    org.voltdb.benchmark.tpcc.procedures.delivery.class };

    /**
     * Constructor needed for JUnit. Should just pass on parameters to superclass.
     * @param name The name of the method to test. This is just passed to the superclass.
     */
    public TestInitStartLocalCluster(String name) {
        super(name);
    }

    public void testClusterUp() throws Exception
    {
        Client client = getClient();
        boolean found = false;
        int timeout = -1;
        VoltTable result = client.callProcedure("@SystemInformation", "DEPLOYMENT").getResults()[0];
        while (result.advanceRow()) {
            if (result.getString("PROPERTY").equalsIgnoreCase("heartbeattimeout")) {
                found = true;
                timeout = Integer.valueOf(result.getString("VALUE"));
            }
        }
        assertTrue(found);
        assertEquals(org.voltcore.common.Constants.DEFAULT_HEARTBEAT_TIMEOUT_SECONDS, timeout);
    }

    static public Test suite() throws Exception {
        // the suite made here will all be using the tests from this class
        MultiConfigSuiteBuilder builder = new MultiConfigSuiteBuilder(TestInitStartLocalCluster.class);

        // get a server config for the native backend with one sites/partitions
        VoltServerConfig config = new LocalCluster("catalogupdate-cluster-base.jar", SITES_PER_HOST, HOSTS, K, BackendTarget.NATIVE_EE_JNI);
        ((LocalCluster)config).setHasLocalServer(false);
        // build up a project builder for the workload
        TPCCProjectBuilder project = new TPCCProjectBuilder();
        project.addDefaultSchema();
        project.addDefaultPartitioning();
        project.addProcedures(BASEPROCS);
        // build the jarfile
        boolean basecompile = config.compile(project);
        assertTrue(basecompile);
        builder.addServerConfig(config);
        return builder;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
}
