package dev.clares.chatgames.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Keeps Minecraft keyboard navigation and narration, with a consistent flat surface. */
final class FlatButton extends Button {
    private boolean selected;
    void selected(boolean value){selected=value;}
    FlatButton(int x,int y,int w,int h,Component text,OnPress action){super(x,y,w,h,text,action,DEFAULT_NARRATION);}
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
        int x=getX(),y=getY();boolean focus=active&&(isHovered()||isFocused());
        g.fill(x,y,x+width,y+height,!active?0xFF172230:focus?0xFF355366:selected?0xFF244B52:0xFF263A4C);
        g.fill(x,y+height-1,x+width,y+height,focus||selected?0xFF81E5D7:0xFF436075);
        if(focus){g.fill(x,y,x+width,y+1,0xFF81E5D7);g.fill(x,y,x+1,y+height,0xFF81E5D7);g.fill(x+width-1,y,x+width,y+height,0xFF81E5D7);}
        var font=Minecraft.getInstance().font;String text=getMessage().getString();
        String clipped=font.width(text)<=width-10?text:font.plainSubstrByWidth(text,Math.max(0,width-18))+"…";
        int color=getMessage().getStyle().getColor()==null?GuiTheme.TEXT:0xFF000000|getMessage().getStyle().getColor().getValue();
        g.text(font,clipped,x+(width-font.width(clipped))/2,y+(height-8)/2,!active?0xFF6F8295:focus||selected?0xFFB4FFF1:color);
    }
    @Override protected void extractDefaultLabel(ActiveTextCollector collector) { }
}
