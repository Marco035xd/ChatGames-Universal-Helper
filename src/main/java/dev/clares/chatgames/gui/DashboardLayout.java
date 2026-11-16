package dev.clares.chatgames.gui;

public record DashboardLayout(int x,int y,int width,int height) {
    public static DashboardLayout of(int width,int height){int w=Math.min(720,width-16),h=Math.min(430,height-16);return new DashboardLayout((width-w)/2,(height-h)/2,w,h);}
    public int rows(){return Math.max(1,(height-132)/58);}
    public int pageSize(){return rows()*2;}
    public int cardWidth(){return (width-32)/2;}
    public int cardX(int slot){return x+12+(slot%2)*(cardWidth()+8);}
    public int cardY(int slot){return y+102+(slot/2)*58;}
    public int footer(){return y+height-24;}
}
