/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.test;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Having trouble with your {@link Process}'s output and error streams?
 * Are they getting filled up and your main thread hangs? Fear no more. Use this
 * class to bundle everything up in one nifty little object that handles all
 * your needs. Use the {@code ProcessStreamHandler#launch()} method to start
 * the consumer threads and once you are {@code ProcessStreamHandler#done()}
 * just say so.
 */
public class ProcessStreamHandler
{
    private final Thread out;
    private final Thread err;
    private Process process;

    /**
     * Convenience constructor assuming the local output streams are
     * {@link System.out} and {@link System.err} for the process's OutputStream
     * and ErrorStream respectively.
     * 
     * Set quiet to true if you just want to consume the output to avoid locking up the process.
     *
     * @param process The process whose output to consume.
     */
    public ProcessStreamHandler( Process process, boolean quiet )
    {
        this.process = process;
        out = new Thread( new StreamConsumer( process.getInputStream(), System.out, quiet ) );
        err = new Thread( new StreamConsumer( process.getErrorStream(), System.err, quiet ) );
    }

    /**
     * Starts the consumer threads. Calls {@link Thread#start()}.
     */
    public void launch()
    {
        out.start();
        err.start();
    }

    /**
     * Joins with the consumer Threads. Calls {@link Thread#join()} on the two
     * consumers.
     */
    public void done()
    {
        try
        {
            out.join();
        }
        catch( InterruptedException e )
        {
            Thread.interrupted();
            e.printStackTrace();
        }
        try
        {
            err.join();
        }
        catch( InterruptedException e )
        {
            Thread.interrupted();
            e.printStackTrace();
        }
    }

    public void cancel()
    {
        out.interrupt();

        err.interrupt();
    }
    
    public int waitForResult()
    {
        launch();
        try
        {
            try
            {
                return process.waitFor();
            }
            catch( InterruptedException e )
            {
                Thread.interrupted();
                return 0;
            }
        }
        finally
        {
            done();
        }
    }

    public int waitForResult( int maxSeconds )
    {
        launch();
        try
        {
            long endTime = currentTimeMillis() + SECONDS.toMillis( maxSeconds );
            while ( currentTimeMillis() < endTime )
            {
                try
                {
                    return process.exitValue();
                }
                catch ( IllegalThreadStateException e )
                {
                    // Not beautiful, but we'd like to get away from p.waitFor() (blocking forever).
                    try
                    {
                        Thread.sleep( 100 );
                    }
                    catch ( InterruptedException ie )
                    {
                        throw new RuntimeException( ie );
                    }
                }
            }
            process.destroy();
            throw new RuntimeException( "Process " + process + " didn't exit by itself within " + maxSeconds + " seconds so I killed it" );
        }
        finally
        {
            done();
        }
    }
}
