package com.shizukumanager.app.shizuku.aidl;

// Giao dien AIDL chay trong tien trinh Shizuku UserService (UID 2000/0)
interface IShizukuShellService {
    void destroy() = 16777114;
    String executeCommand(in String[] commandArgs) = 1;
    int getServiceUid() = 2;
}
