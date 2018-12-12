package com.wuhf.findviews;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.wuhf.findviews.beans.ResIdBean;
import com.wuhf.findviews.utils.ClassDataWriter;
import com.wuhf.findviews.utils.PsiFileUtils;

import java.util.ArrayList;

public class FindViewsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
        PsiElement psiElement = PsiFileUtils.getPsiElementByEditor(editor, psiFile);
        if (editor != null && psiFile != null && psiElement != null) {
            String name = String.format("%s.xml", psiElement.getText());
            PsiFile rootXmlFile = PsiFileUtils.getFileByName(psiElement, name);
            if (rootXmlFile != null) {
                ArrayList<ResIdBean> resIdBeans = new ArrayList<>();
                PsiFileUtils.getResIdBeans(rootXmlFile, resIdBeans);
                PsiClass psiClass = PsiFileUtils.getClassByClassFile(psiFile);
                new ClassDataWriter(anActionEvent, psiFile, resIdBeans, psiClass).execute();
            }
        }
    }
}
