package dev.clares.chatgames.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;

/** A keyboard-accessible Minecraft button with a decorative game item. */
final class GameCard extends Button {
    private final ItemStack icon;
    private final String time,source;
    GameCard(int x,int y,int width,GameCatalog.Entry entry,String time,String source,OnPress action) {
        super(x,y,width,32,net.minecraft.network.chat.Component.literal(entry.title()),action,DEFAULT_NARRATION);
        this.time=time;this.source=source;
        var client=Minecraft.getInstance();
        icon=client.level==null?null:new ItemStack(switch(entry.type()) {
            case MATH->Items.REDSTONE;case VARIABLE->Items.COMPARATOR;case TRIVIA->Items.BOOK;
            case UNSCRAMBLE->Items.NAME_TAG;case FILLOUT->Items.WRITABLE_BOOK;case UNREVERSE->Items.SPYGLASS;
            case REACTION->Items.FEATHER;case RANDOM->Items.PAPER;case CLICKABLE->Items.STONE_BUTTON;
            default->Items.ENDER_PEARL;
        });
    }
    @Override protected void extractContents(GuiGraphicsExtractor c,int mouseX,int mouseY,float delta) {
        boolean selected=isHovered()||isFocused();int x=getX(),y=getY();
        c.fill(x,y,x+width,y+height,selected?0xFF30475B:0xFF203044);
        c.fill(x,y,x+2,y+height,selected?0xFF81E5D7:0xFF4C7C83);
        if(icon!=null)c.item(icon,x+8,y+9);else c.text(Minecraft.getInstance().font,"◆",x+10,y+12,GuiTheme.GOLD);
        var r=Minecraft.getInstance().font;
        GuiTheme.fit(c,r,getMessage().getString(),x+30,y+8,width-34,GuiTheme.TEXT);
        GuiTheme.fit(c,r,time,x+30,y+22,width-34,GuiTheme.GOLD);
        GuiTheme.fit(c,r,source,x+8,y+38,width-66,GuiTheme.MUTED);
    }
    @Override protected void extractDefaultLabel(ActiveTextCollector consumer){}
}
