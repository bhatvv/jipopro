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

package org.jitsi.recording.postprocessing.overlay;

import java.awt.*;
import java.awt.geom.*;

import org.jitsi.recording.postprocessing.overlay.manager.*;

/**
 * Interface that represents an item that is overlayed on a single participant
 * video tile (e.g. text, image, avatar etc.)
 * @author Vladimir Marinov
 */
public abstract class Overlay {
    /** The dimension of the video tile */
    protected Dimension dimension;
    
    /** The position of the video tile */
    protected Point2D.Double position;
    
    /**
     * Set the dimension (width/height) of the current overlay
     * @param dimension the overlay dimension
     */
    public void setDimension(Dimension dimension)
    {
        this.dimension = dimension;
    }
    
    /**
     * Set the position of this overlay
     * @param position the overlay position
     */
    public void setPosition(Point2D.Double position)
    {
        this.position = position;
    }
    
    /**
     * Gets the {@link String} that we need to pass to FFMPEG in order to 
     * perform the actual video overlaying. For example (a text overlay):
     * drawtext=fontfile=/usr/share/fonts/truetype/DroidSans.ttf: 
     * x=(w-tw)/2: y=h-(2*lh): fontcolor=white: box=1: boxcolor=0x00000000@1
     * @return the ffmpeg video filter (-vf) string. The returned {@link String}
     * should allow the {@link OverlaysManager} that uses this instance choose
     * aliases of its choice, i.e. choose names for input and output nodes of
     * this video filter. For this reason the result {@link String} should
     * have %s instead of aliases, e.g.
     * movie=logo.png [logo];[%s][logo]overlay=0:0 [%s];
     * @return the {@link String} that we need to pass to FFMPEG in order to 
     * perform the actual video overlaying.
     */
    public abstract String getFFMPEGVideoFilterString();
}
