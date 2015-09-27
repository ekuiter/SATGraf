/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package com.satgraf.graph.placer;

import com.satgraf.graph.placer.ForceFactory.RepulsionForce;
import com.satlib.graph.Node;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

;

/**
 *
 * @author Mathieu Jacomy
 */
public class NodesThread implements Callable{

    private Map<Node, ForceAtlas2LayoutData> map;
    private List<Node> nodes;
    private int from;
    private int to;
    private Region rootRegion;
    private boolean barnesHutOptimize;
    private RepulsionForce Repulsion;
    private double barnesHutTheta;
    private double gravity;
    private RepulsionForce GravityForce;
    private double scaling;
    private double progress;

    public NodesThread(List<Node> nodes, Map<Node, ForceAtlas2LayoutData> map, int from, int to, boolean barnesHutOptimize, double barnesHutTheta, double gravity, RepulsionForce GravityForce, double scaling, Region rootRegion, RepulsionForce Repulsion) {
        this.nodes = nodes;
        this.map = map;
        this.from = from;
        this.to = to;
        this.rootRegion = rootRegion;
        this.barnesHutOptimize = barnesHutOptimize;
        this.Repulsion = Repulsion;
        this.barnesHutTheta = barnesHutTheta;
        this.gravity = gravity;
        this.GravityForce = GravityForce;
        this.scaling = scaling;
    }

    @Override
    public Object call() {
        // Repulsion
        if (barnesHutOptimize) {
            for (int nIndex = from; nIndex < to; nIndex++) {
                Node n = nodes.get(nIndex);
                rootRegion.applyForce(n, map.get(n), Repulsion, barnesHutTheta);
                progress = ((double)nIndex/(double)(to-from))/2.0;
            }
        } else {
            for (int n1Index = from; n1Index < to; n1Index++) {
                Node n1 = nodes.get(n1Index);
                for (int n2Index = 0; n2Index < n1Index; n2Index++) {
                    Node n2 = nodes.get(n2Index);
                    Repulsion.apply(n1, map.get(n1), n2, map.get(n2));
                    progress = ((double)n1Index/(double)(to-from))/2.0;
                }
            }
        }

        // Gravity
        for (int nIndex = from; nIndex < to; nIndex++) {
            Node n = nodes.get(nIndex);
            GravityForce.apply(n, map.get(n), gravity / scaling);
            progress = 0.5+(((double)nIndex/(double)(to-from))/2.0);
        }
        return null;
    }
    
    public double getProgress(){
        return progress;
    }
}