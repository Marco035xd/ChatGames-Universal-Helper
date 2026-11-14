package dev.clares.chatgames.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

final class GuiTheme {
    static final int TEXT=0xFFE8F1F5,MUTED=0xFF9DAFBC,GOLD=0xFFFFCF70,GREEN=0xFF8BE0AD,RED=0xFFFF9999;
    static void panel(GuiGraphicsExtractor c,int x,int y,int w,int h) {
        c.fill(x-2,y-2,x+w+2,y+h+2,0xCC03080F);
        c.fill(x,y,x+w,y+h,0xF0172233);
        c.fill(x,y,x+w,y+2,0xFF5EC6BD);
        c.fill(x,y+34,x+w,y+35,0xFF2C3C50);
    }
    static void fit(GuiGraphicsExtractor c,Font r,String s,int x,int y,int width,int color) {
        String clipped=r.width(s)<=width?s:r.plainSubstrByWidth(s,Math.max(0,width-r.width("…")))+"…";
        c.text(r,clipped,x,y,color);
    }
    static Component text(String s){return Component.literal(s);}
    private GuiTheme(){}
}
