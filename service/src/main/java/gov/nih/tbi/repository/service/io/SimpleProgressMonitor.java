package gov.nih.tbi.repository.service.io;

import com.jcraft.jsch.SftpProgressMonitor;

public class SimpleProgressMonitor implements SftpProgressMonitor {
    private double count;
    private double max;
    private String src;
    private int percent;
    private int lastDisplayedPercent; 

    public SimpleProgressMonitor() {
        count = 0;
        max = 0;
        percent = 0;
        lastDisplayedPercent = 0;
    }
    public void init(int op, String src, String dest, long max) {
        this.max = max;
        this.src = src;
        count = 0;
        percent = 0;
        lastDisplayedPercent = 0;
        status();
    }
    public boolean count(long count) {
        this.count += count;
        percent = (int) ((this.count / max) * 100.0);
        status();
        return true;
    } 
    public void end() {
        percent = (int) ((count / max) * 100.0);
        status();
    } 
    private void status() {
        if (lastDisplayedPercent <= percent - 10) {
            //System.out.println(src + ": " + percent + "% " + ((long) count) + "/" + ((long) max));
            lastDisplayedPercent = percent;
        }
    }
} 