package com.example.BlockchainServer.block;

import java.util.List;

public interface BlockService {
    void save(Block block);
    List<Block> getBlockchain();
    Block getLastBlock();
    void removeLastBlock();
}
