/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.swing.ConfirmCheckBoxDialog;
import javax.swing.*;

/** @version $Id$ */
public class ConfigOptionListeners implements OptionConstants {
  public static class SlaveJVMArgsListener implements OptionListener<String>, OptionConstants {
    protected JFrame _parent;
    public SlaveJVMArgsListener(JFrame parent) { _parent = parent; }
    public void optionChanged(OptionEvent<String> oe) {
      final OptionListener<String> slaveJvmArgsListener = this;
      if (!oe.value.equals("")) {
        int result = JOptionPane.
          showConfirmDialog(_parent,
                            "Specifying Interations JVM Args is an advanced option. Invalid arguments may cause\n" +
                            "the Interactions Pane to stop working.\n" + "Are you sure you want to set this option?\n" +
                            "(You will have to reset the interactions pane before changes take effect.)",
                            "Confirm Interactions JVM Arguments", JOptionPane.YES_NO_OPTION);
        if (result!=JOptionPane.YES_OPTION) {
          DrJava.getConfig().setSetting(oe.option, "");
        }
        else {
          sanitizeSlaveJVMArgs(_parent, oe.value, this);
        }
      }
    }
  }
  
  @SuppressWarnings("fallthrough")
  public static void sanitizeSlaveJVMArgs(JFrame parent,
                                          String value,
                                          final OptionListener<String> l) {
    int pos = value.indexOf("-Xmx");
    if (((pos>1) && (Character.isWhitespace(value.charAt(pos-1)))) ||
        (pos==0)) {
      int endpos = pos+("-Xmx".length());
      while((endpos<value.length()) &&
            (!Character.isWhitespace(value.charAt(endpos)))) {
        ++endpos;
      }
      
      int startpos = pos+("-Xmx".length());
      String size = value.substring(startpos,endpos);
      long factor = 1;
      long heapSize;
      switch(size.toLowerCase().charAt(size.length()-1)) {
        case 'g': {
          factor *= 1024; // fall-through intended
        }
        case 'm': {
          factor *= 1024; // fall-through intended
        }
        case 'k': {
          factor *= 1024; // fall-through intended
          break;
        }
        default: {
          if (!Character.isDigit(size.toLowerCase().charAt(size.length()-1))) {
            factor = 0;
          }
        }
      }
      try {
        if (factor==1) {
          heapSize = new Long(size);
        }
        else if (factor>1) {
          heapSize = new Long(size.substring(0,size.length()-1)) * factor;
        }
        else {
          heapSize = -1;
        }
      }
      catch(NumberFormatException nfe) {
        heapSize = -1; // invalid
      }
      int result;
      if (heapSize>=0) {
        String[] options = new String[] { "Copy to \"Maximum Heap\" Setting",
          "Clean \"Slave JVM Args\"",
          "Ignore" };
        result = JOptionPane.
          showOptionDialog(parent,
                           "You seem to have specified the maximum heap size as part of the\n" +
                           "\"JVM Args for Interactions JVM\" setting: \"-Xmx"+size+"\"\n"+
                           "The \"Maximum Heap Memory for Interactions JVM\" setting should be used instead.\n"+
                           "Would you like to copy the value\n\""+size+"\" into the \"Maximum Heap\" setting,\n"+
                           "just clean up \"JVM Args for Interactions JVM\", or ignore this potential problem?",
                           "Maximum Heap Size Set in JVM Arguments",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
      }
      else {
        String[] options = new String[] { "Clean \"Main JVM Args\"",
          "Ignore" };
        result = JOptionPane.
          showOptionDialog(parent,
                           "You seem to have specified the maximum heap size as part of the\n" +
                           "\"JVM Args for Interactions JVM\" setting: \"-Xmx"+size+"\"\n"+
                           "The \"Maximum Heap Memory for Interactions JVM\" setting should be used instead.\n"+
                           "Furthermore, the specified heap size \""+size+"\" is invalid.\n"+
                           "Would you like to clean up the \"JVM Args for Interactions JVM\", copy the value\n"+
                           "\""+size+"\" into the \"Maximum Heap\" setting,\n"+
                           "or ignore this potential problem?",
                           "Maximum Heap Size Set in JVM Arguments",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
        if (result==1) { result = 2; }
      }
      if (result!=2) {
        // clean up
        while((endpos<value.length()) &&
              (Character.isWhitespace(value.charAt(endpos)))) {
          ++endpos;
        }
        String newValue = value.substring(0,pos) + value.substring(endpos);
        DrJava.getConfig().removeOptionListener(SLAVE_JVM_ARGS, l);
        DrJava.getConfig().addOptionListener(SLAVE_JVM_ARGS, new OptionListener<String>() {
          public void optionChanged(OptionEvent<String> oe) {
            DrJava.getConfig().removeOptionListener(SLAVE_JVM_ARGS, l);
            SwingUtilities.invokeLater(new Runnable() { 
              public void run() {
                DrJava.getConfig().addOptionListener(SLAVE_JVM_ARGS, l);
              }
            });
          }
        });
        DrJava.getConfig().setSetting(SLAVE_JVM_ARGS, newValue);
        if (result==0) {
          // copy
          DrJava.getConfig().setSetting(SLAVE_JVM_XMX, size);
        }
      }
    }
  }
  
  public static class SlaveJVMXMXListener implements OptionListener<String>, OptionConstants {
    protected JFrame _parent;
    public SlaveJVMXMXListener(JFrame parent) { _parent = parent; }
    @SuppressWarnings("fallthrough")
    public void optionChanged(OptionEvent<String> oe) {
      sanitizeSlaveJVMXMX(_parent, oe.value);
      JOptionPane.showMessageDialog(_parent,
                                    "You will have to reset the interactions pane before changes take effect.");
    }
  }
  
  @SuppressWarnings("fallthrough")
  public static void sanitizeSlaveJVMXMX(JFrame parent, String value) {
    if (!value.equals("")) {
      String size = value.trim();
      long factor = 1;
      long heapSize;
      switch(size.toLowerCase().charAt(size.length()-1)) {
        case 'g': {
          factor *= 1024; // fall-through intended
        }
        case 'm': {
          factor *= 1024; // fall-through intended
        }
        case 'k': {
          factor *= 1024; // fall-through intended
          break;
        }
        default: {
          if (!Character.isDigit(size.toLowerCase().charAt(size.length()-1))) {
            factor = 0;
          }
        }
      }
      try {
        if (factor==1) {
          heapSize = new Long(size);
        }
        else if (factor>1) {
          heapSize = new Long(size.substring(0,size.length()-1)) * factor;
        }
        else {
          heapSize = -1;
        }
      }
      catch(NumberFormatException nfe) {
        heapSize = -1; // invalid
      }
      if (heapSize<0) {
        String[] options = new String[] { "Clean \"Maximum Heap\" Setting",
          "Ignore" };
        int result = JOptionPane.
          showOptionDialog(parent,
                           "The \"Maximum Heap Memory for Interactions JVM\" setting is invalid: \""+size+"\"\n"+
                           "Would you like to clean up the \"Maximum Heap\" setting or ignore this potential problem?",
                           "Invalid Maximum Heap Size",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
        if (result==0) {
          // clean up
          DrJava.getConfig().setSetting(SLAVE_JVM_XMX, "");
        }
      }
    }
  }
  
  public static class MasterJVMArgsListener implements OptionListener<String>, OptionConstants {
    protected JFrame _parent;
    public MasterJVMArgsListener(JFrame parent) { _parent = parent; }
    public void optionChanged(OptionEvent<String> oe) {
      final OptionListener<String> masterJvmArgsListener = this;
      if (!oe.value.equals("")) {
        int result = JOptionPane.
          showConfirmDialog(_parent,
                            "Specifying Main JVM Args is an advanced option. Invalid arguments may cause\n" +
                            "DrJava to fail on start up.  You may need to edit or delete your .drjava preferences file\n" +
                            "to recover.\n Are you sure you want to set this option?\n" +
                            "(You will have to restart Drjava before changes take effect.)",
                            "Confirm Main JVM Arguments", JOptionPane.YES_NO_OPTION);
        if (result!=JOptionPane.YES_OPTION) {
          DrJava.getConfig().setSetting(oe.option, "");
        }
        else {
          sanitizeMasterJVMArgs(_parent, oe.value, this);
        }
      }
    }
  }
  
  @SuppressWarnings("fallthrough")
  public static void sanitizeMasterJVMArgs(JFrame parent,
                                           String value,
                                           final OptionListener<String> l) {
    int pos = value.indexOf("-Xmx");
    if (((pos>1) && (Character.isWhitespace(value.charAt(pos-1)))) ||
        (pos==0)) {
      int startpos = pos+("-Xmx".length());
      int endpos = startpos;
      while((endpos<value.length()) &&
            (!Character.isWhitespace(value.charAt(endpos)))) {
        ++endpos;
      }

      String size = value.substring(startpos,endpos);
      long factor = 1;
      long heapSize;
      switch(size.toLowerCase().charAt(size.length()-1)) {
        case 'g':
          factor *= 1024; // fall-through intended
        case 'm':
          factor *= 1024; // fall-through intended
        case 'k':
          factor *= 1024; // fall-through intended
        default:
          try{
          heapSize = new Long(size.substring(0,size.length()-2)) * factor;
        }
          catch(NumberFormatException nfe) {
            heapSize = -1; // invalid
          }
      }
      int result;
      if (heapSize>=0) {
        String[] options = new String[] { "Copy to \"Maximum Heap\" Setting",
          "Clean \"Main JVM Args\"",
          "Ignore" };
        result = JOptionPane.
          showOptionDialog(parent,
                           "You seem to have specified the maximum heap size as part of the\n" +
                           "\"JVM Args for Main JVM\" setting: \"-Xmx"+size+"\"\n"+
                           "The \"Maximum Heap Memory for Main JVM\" setting should be used instead.\n"+
                           "Would you like to copy the value\n\""+size+"\" into the \"Maximum Heap\" setting,\n"+
                           "just clean up \"JVM Args for Main JVM\", or ignore this potential problem?",
                           "Maximum Heap Size Set in JVM Arguments",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
      }
      else {
        String[] options = new String[] { "Clean \"Main JVM Args\"",
          "Ignore" };
        result = JOptionPane.
          showOptionDialog(parent,
                           "You seem to have specified the maximum heap size as part of the\n" +
                           "\"JVM Args for Main JVM\" setting: \"-Xmx"+size+"\"\n"+
                           "The \"Maximum Heap Memory for Main JVM\" setting should be used instead.\n"+
                           "Furthermore, the specified heap size \""+size+"\" is invalid.\n"+
                           "Would you like to clean up the \"JVM Args for Main JVM\"\n"+
                           "or ignore this potential problem?",
                           "Maximum Heap Size Set in JVM Arguments",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
        if (result==1) { result = 2; }
      }
      if (result!=2) {
        // clean up
        while((endpos<value.length()) &&
              (Character.isWhitespace(value.charAt(endpos)))) {
          ++endpos;
        }
        String newValue = value.substring(0,pos) + value.substring(endpos);
        DrJava.getConfig().removeOptionListener(MASTER_JVM_ARGS, l);
        DrJava.getConfig().addOptionListener(MASTER_JVM_ARGS, new OptionListener<String>() {
          public void optionChanged(OptionEvent<String> oe) {
            DrJava.getConfig().removeOptionListener(MASTER_JVM_ARGS, this);
            SwingUtilities.invokeLater(new Runnable() { 
              public void run() {
                DrJava.getConfig().addOptionListener(MASTER_JVM_ARGS, l);
              }
            });
          }
        });
        DrJava.getConfig().setSetting(MASTER_JVM_ARGS, newValue);
        if (result==0) {
          // copy
          DrJava.getConfig().setSetting(MASTER_JVM_XMX, size);
        }
      }
    }
  }
  
  public static class MasterJVMXMXListener implements OptionListener<String>, OptionConstants {
    protected JFrame _parent;
    public MasterJVMXMXListener(JFrame parent) { _parent = parent; }
    public void optionChanged(OptionEvent<String> oe) {
      sanitizeMasterJVMXMX(_parent, oe.value);
    }
  }
  
  @SuppressWarnings("fallthrough")
  public static void sanitizeMasterJVMXMX(JFrame parent, String value) {
    if (!value.equals("")) {
      String size = value.trim();
      long factor = 1;
      long heapSize;
      switch(size.toLowerCase().charAt(size.length()-1)) {
        case 'g': {
          factor *= 1024; // fall-through intended
        }
        case 'm': {
          factor *= 1024; // fall-through intended
        }
        case 'k': {
          factor *= 1024; // fall-through intended
          break;
        }
        default: {
          if (!Character.isDigit(size.toLowerCase().charAt(size.length()-1))) {
            factor = 0;
          }
        }
      }
      try {
        if (factor==1) {
          heapSize = new Long(size);
        }
        else if (factor>1) {
          heapSize = new Long(size.substring(0,size.length()-1)) * factor;
        }
        else {
          heapSize = -1;
        }
      }
      catch(NumberFormatException nfe) {
        heapSize = -1; // invalid
      }
      if (heapSize<0) {
        String[] options = new String[] { "Clean \"Maximum Heap\" Setting",
          "Ignore" };
        int result = JOptionPane.
          showOptionDialog(parent,
                           "The \"Maximum Heap Memory for Main JVM\" setting is invalid: \""+size+"\"\n"+
                           "Would you like to clean up the \"Maximum Heap\" setting or ignore this potential problem?",
                           "Invalid Maximum Heap Size",
                           0,
                           JOptionPane.QUESTION_MESSAGE,
                           null,
                           options,
                           options[0]);
        if (result==0) {
          // clean up
          DrJava.getConfig().setSetting(MASTER_JVM_XMX, "");
        }
      }
    }
  }
  
  public static class JavadocCustomParamsListener implements OptionListener<String>, OptionConstants {
    protected JFrame _parent;
    public JavadocCustomParamsListener(JFrame parent) { _parent = parent; }
    public void optionChanged(OptionEvent<String> oe) {
      sanitizeJavadocCustomParams(_parent, oe.value);
    }
  }
  
  @SuppressWarnings("fallthrough")
  public static void sanitizeJavadocCustomParams(JFrame parent,
                                                 String value) {
    boolean containsPrivate = (value.indexOf("-private")>=0);
    boolean containsProtected = (value.indexOf("-protected")>=0);
    boolean containsPublic = (value.indexOf("-public")>=0);
    boolean containsPackage = (value.indexOf("-package")>=0);

    if (containsPrivate || containsProtected || containsPublic || containsPackage) {
      StringBuilder sb = new StringBuilder();
      if (containsPublic) { sb.append("-public "); }
      if (containsProtected) { sb.append("-protected "); }
      if (containsPrivate) { sb.append("-private "); }
      if (containsPackage) { sb.append("-package "); }      
      String[] options = new String[] { "Copy to \"Access Level\" Setting",
        "Clean \"Custom Javadoc Parameters\"",
        "Ignore" };
      int result = JOptionPane.
        showOptionDialog(parent,
                         "You seem to have specified "+sb.toString()+"as part of the\n" +
                         "\"Custom Javadoc Parameters\" setting. The \"Access Level\"\n"+
                         "setting should be used instead. Would you like to copy the\n"+
                         "parameter into the \"Access Level\" setting, just clean up\n"+
                         "\"Custom Javadoc Parameters\", or ignore this potential problem?",
                         "Access Level Set in Custom Javadoc Parameters",
                         0,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         options,
                         options[0]);
      if (result!=2) {
        if (result==0) {
          // copy
          if (containsPublic) { DrJava.getConfig().setSetting(JAVADOC_ACCESS_LEVEL, "public"); }
          else if (containsProtected) { DrJava.getConfig().setSetting(JAVADOC_ACCESS_LEVEL, "protected"); }
          else if (containsPrivate) { DrJava.getConfig().setSetting(JAVADOC_ACCESS_LEVEL, "private"); }
          else if (containsPackage) { DrJava.getConfig().setSetting(JAVADOC_ACCESS_LEVEL, "package"); }
        }
        // clean up
        String[] params = value.split("(-private|-protected|-package|-public)");
        sb = new StringBuilder();
        for(int i=0;i<params.length;i++){
          if(!params[i].trim().equals("")) { sb.append(params[i].trim()); sb.append(' '); }
        }
        DrJava.getConfig().setSetting(JAVADOC_CUSTOM_PARAMS, sb.toString().trim());
      }
    }
  }
  
  public static class LookAndFeelListener implements OptionListener<String> {
    protected JFrame _parent;
    public LookAndFeelListener(JFrame parent) { _parent = parent; }
    public void optionChanged(OptionEvent<String> oe) {
//        try {
//          UIManager.setLookAndFeel(oe.value);
//          SwingUtilities.updateComponentTreeUI(MainFrame.this);
//          if (_debugPanel != null) {
//            SwingUtilities.updateComponentTreeUI(_debugPanel);
//          }
//          if (_configFrame != null) {
//            SwingUtilities.updateComponentTreeUI(_configFrame);
//          }
//          if (_helpFrame != null) {
//            SwingUtilities.updateComponentTreeUI(_helpFrame);
//          }
//          if (_aboutDialog != null) {
//            SwingUtilities.updateComponentTreeUI(_aboutDialog);
//          }
//          SwingUtilities.updateComponentTreeUI(_navPanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_interactionsPanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_consolePanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_openChooser);
//          SwingUtilities.updateComponentTreeUI(_saveChooser);
//          Iterator<TabbedPanel> it = _tabs.iterator();
//          while (it.hasNext()) {
//            SwingUtilities.updateComponentTreeUI(it.next());
//          }
//        }
//        catch (Exception ex) {
//          _showError(ex, "Could Not Set Look and Feel",
//                     "An error occurred while trying to set the look and feel.");
//        }
      
      String title = "Apply Look and Feel";
      String msg = "Look and feel changes will take effect when you restart DrJava.";
      if (DrJava.getConfig().getSetting(WARN_CHANGE_LAF).booleanValue()) {
        ConfirmCheckBoxDialog dialog =
          new ConfirmCheckBoxDialog(_parent, title, msg,
                                    "Do not show this message again",
                                    JOptionPane.INFORMATION_MESSAGE,
                                    JOptionPane.DEFAULT_OPTION);
        if (dialog.show() == JOptionPane.OK_OPTION && dialog.getCheckBoxValue()) {
          DrJava.getConfig().setSetting(WARN_CHANGE_LAF, Boolean.FALSE);
        }
      }
    }
  }  
}