package net.metacraft.mod.api;

public interface ISchemeResponseData {

    byte[] getDataArray();
    int getBytesToRead();
    void setAmountRead(int rd);

}
