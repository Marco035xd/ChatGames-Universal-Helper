package dev.clares.chatgames.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Paginated, wrapped detail view, including at the smallest supported GUI scale. */
class InfoScreen extends Screen {
    private final Screen parent;
    private final List<String> paragraphs;
    private final List<String> lines=new ArrayList<>();
    private int x,y,w,h,page,rows;
    InfoScreen(Screen parent,String title,List<String> paragraphs){super(Component.literal(title));this.parent=parent;this.paragraphs=List.copyOf(paragraphs);}
    @Override protected void init() {
        w=Math.min(580,width-24);h=Math.min(360,height-16);x=(width-w)/2;y=(height-h)/2;rows=Math.max(1,(h-72)/13);
        lines.clear();
        for(String paragraph:paragraphs){
            String rest=paragraph;
            if(rest.isEmpty())lines.add("");
            while(!rest.isEmpty()) {String part=font.plainSubstrByWidth(rest,w-28);if(part.isEmpty())part=rest.substring(0,1);if(part.length()<rest.length()&&part.lastIndexOf(' ')>0)part=part.substring(0,part.lastIndexOf(' '));lines.add(part);rest=rest.substring(part.length()).stripLeading();}
        }
        page=Math.min(page,Math.max(0,(lines.size()-1)/rows));
        addRenderableWidget(GuiTheme.button("‹",x+12,y+h-28,28,20,()->{page--;rebuildWidgets();},"Anterior")).active=page>0;
        addRenderableWidget(GuiTheme.button("›",x+44,y+h-28,28,20,()->{page++;rebuildWidgets();},"Siguiente")).active=(page+1)*rows<lines.size();
        addRenderableWidget(GuiTheme.button("Volver",x+w-90,y+h-28,78,20,this::onClose,"Vuelve al panel sin cambiar ajustes."));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xC9080E19);GuiTheme.panel(g,x,y,w,h);
        GuiTheme.fit(g,font,title.getString(),x+12,y+10,w-24,GuiTheme.GOLD);
        g.text(font,"Detalles · "+(page+1)+" / "+Math.max(1,(lines.size()+rows-1)/rows),x+12,y+24,GuiTheme.MUTED);
        for(int i=page*rows;i<Math.min(lines.size(),(page+1)*rows);i++)g.text(font,lines.get(i),x+12,y+43+(i-page*rows)*13,GuiTheme.TEXT);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
