package dev.tronxi.papayaclient.files.papayastatusfile;

import java.util.HashSet;
import java.util.Set;

public class PartStatusFile {
    private String fileName;
    private String fileHash;
    private PapayaStatus status;
    Set<PartPeerStatusFile> partPeerStatusFiles;

    public PartStatusFile(String fileName, String fileHash, PapayaStatus status) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.status = status;
        this.partPeerStatusFiles = new HashSet<>();
    }

    public PartStatusFile() {

    }

    public String getFileHash() {
        return fileHash;
    }

    public PartStatusFile setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public PartStatusFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public PapayaStatus getStatus() {
        return status;
    }

    public PartStatusFile setStatus(PapayaStatus status) {
        this.status = status;
        return this;
    }

    public Set<PartPeerStatusFile> getPartPeerStatusFiles() {
        return partPeerStatusFiles;
    }

    public PartStatusFile setPartPeerStatusFiles(Set<PartPeerStatusFile> partPeerStatusFiles) {
        this.partPeerStatusFiles = partPeerStatusFiles;
        return this;
    }

    public void addPeer(PartPeerStatusFile partPeerStatusFile) {
        this.partPeerStatusFiles.add(partPeerStatusFile);
    }

    @Override
    public String toString() {
        return "PartStatusFile{" +
                "fileName='" + fileName + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", status=" + status +
                ", partPeerStatusFiles=" + partPeerStatusFiles +
                '}';
    }
}
