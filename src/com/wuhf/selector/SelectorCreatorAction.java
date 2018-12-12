package com.wuhf.selector;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.wuhf.selector.utils.Log;

import java.util.regex.Matcher;

public class SelectorCreatorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Log.d("---- Start - menu item clicked ----");
//        VirtualFile selectedFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
        for (VirtualFile selectedFile : data) {
            if (isCorrectFolderSelected(selectedFile)) {
                new SelectorDetector(e.getProject()).detectAndCreateSelectors(selectedFile);
                showInfoDialog("Selectors were generated into 'drawable' of 'mipmap' folder", e);
            } else {
                if (selectedFile != null) {
                    showErrorDialog("You need to select folder with image resources, for example 'drawables-xhdpi' or 'mipmap-xhdpi' " +
                            "" + selectedFile.getName(), e);
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile selectedFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        e.getPresentation().setEnabled(isCorrectFolderSelected(selectedFile));
    }

    private boolean isCorrectFolderSelected(VirtualFile selectedFile) {
        if (selectedFile != null && selectedFile.isDirectory()) {
            Matcher matcher = Constants.VALID_FOLDER_PATTERN.matcher(selectedFile.getName());
            Matcher matcher2 = Constants.VALID_FOLDER_MIPMAP_PATTERN.matcher(selectedFile.getName());
            if (matcher.matches() || matcher2.matches()) {
                return true;
            }
        }
        return false;
    }

    private void showInfoDialog(String text, AnActionEvent e) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(DataKeys.PROJECT.getData(e.getDataContext()));

        if (statusBar != null) {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(text, MessageType.INFO, null)
                    .setFadeoutTime(10000)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                            Balloon.Position.atRight);
        }
    }

    private void showErrorDialog(String text, AnActionEvent e) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(DataKeys.PROJECT.getData(e.getDataContext()
        ));

        if (statusBar != null) {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(text, MessageType.ERROR, null)
                    .setFadeoutTime(10000)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                            Balloon.Position.atRight);
        }
    }
}

