/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Indents current line to the indent level of the previous line (ignoring all comments!) augmented by a suffix.
  * @version $Id: ActionStartPrevLinePlus.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
class ActionStartPrevLineSkipCommentsPlus extends IndentRuleAction {
  private String _suffix;

  /** Repeats the indentation from the previous line augmented by a suffix
    * @param suffix  The string to be added
    */
  public ActionStartPrevLineSkipCommentsPlus(String suffix) { 
    super(suffix); // propagate arg information to superclass IndentWithTrace
    _suffix = suffix; 
  }

  /** Indents the line according to the previous line (ignoring all comments), with the suffix string added. On the
    * first line, indent is set to 0.  Only runs in event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return true if the caller should update the current location, false if the indenter has already done it
    */
  public void indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    traceIndenting(doc, reason);  // sets supResult to true, determines _suffix and propagates trace info
    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc._getLineStartPos(here);
     
      // Find prefix of previous line ignoring comment lines 
      String prefix;
      
      // If startLine is not beginning of document, find first 
      if (startLine > 0) {
      
        // Find start of previous line (ignoring comments and empty lines)
        int startPrevLine = doc._getLineStartPos(startLine - 1);
        while (startPrevLine > 0 && doc.isPrevLineIgnorable(startLine)) {
          startLine = startPrevLine;
          startPrevLine = doc._getLineStartPos(startPrevLine - 1);
        }
        
        // if startPrevLine is 0, the following code binds prefix to _suffix
//        System.err.println("*** startPrevLine = " + startPrevLine);
         
        int firstChar = doc._getLineFirstCharPos(startPrevLine);
        prefix = doc.getText(startPrevLine, firstChar - startPrevLine) + _suffix;
      }
      else prefix = _suffix; 
//      System.err.println("Question [ASPLP] prefix.length() = " + prefix.length());
      
      if (AbstractDJDocument.hasOnlySpaces(prefix)) doc.setTab(prefix.length(), here);
      else doc.setTab(prefix, here);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); } // Shouldn't happen
  }
}
