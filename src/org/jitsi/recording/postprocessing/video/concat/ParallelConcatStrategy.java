/*
/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.recording.postprocessing.video.concat;

import java.io.*;
import java.util.concurrent.*;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * A {@link ConcatStrategy} that concatenates video files in parallel. It 
 * concatenates the video files in pairs putting each concat operation in a
 * task queue.
 * @author Vladimir Marinov
 *
 */
public class ParallelConcatStrategy
        implements ConcatStrategy
{
    
    /**
     * The task queue that processes the concatenation of video files pairs
     */
    private ExecutorService concatTaskQueue = 
            Executors.newFixedThreadPool(Config.JIPOPRO_THREADS);

    @Override
    public void concatFiles(
        String sourceDir, String outputFilename)
    {
        sourceDir = sourceDir.isEmpty() ? "." : sourceDir;

        int filesNum = (new File(sourceDir)).listFiles().length;
        int passNum = 0;
        
        if (filesNum == 0)
        {
            return;
        }
        
        while (filesNum > 1)
        {
            for (int i = 0; i < filesNum - 1; i += 2)
            {
                final String concatFilename = sourceDir + "/" +
                    "concat_" + passNum + "_" + i + "_" + (i + 1);
                
                PrintWriter writer;
                try {
                    writer = new PrintWriter(concatFilename);
                    writer.println("file " + passNum + "_" + i + ".mov");
                    writer.println("file " + passNum + "_" + (i + 1) + ".mov");
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                
                final int f_passNum = passNum, f_i = i;
                final String f_sourceDir = sourceDir;
                
                concatTaskQueue.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            Exec.exec(Config.FFMPEG + " -y -f concat -i "
                                + concatFilename + " -c copy "
                                + f_sourceDir + "/" 
                                + (f_passNum + 1) + "_" + (f_i / 2) + ".mov");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            
            concatTaskQueue.shutdown();
            
            try {
                concatTaskQueue.awaitTermination(
                        Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            concatTaskQueue = 
                    Executors.newFixedThreadPool(Config.JIPOPRO_THREADS);
            
            if (filesNum % 2 == 0)
            {
                filesNum /= 2;
            } else
            {
                final String f_sourceDir = sourceDir;
                final int f_filesNum = filesNum, f_passNum = passNum;
                
                concatTaskQueue.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            Exec.exec("mv " 
                                + f_sourceDir + "/" + f_passNum + "_" 
                                + (f_filesNum - 1) + ".mov "
                                + f_sourceDir + "/" + (f_passNum + 1) + "_" 
                                + (f_filesNum / 2) + ".mov");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                
                filesNum = filesNum / 2 + 1;
            }
            
            passNum++;
        }
        
        concatTaskQueue.shutdown();
        try {
            concatTaskQueue.awaitTermination(
                Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            Exec.exec("mv " + sourceDir + "/" + passNum + "_0.mov " 
                + outputFilename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
