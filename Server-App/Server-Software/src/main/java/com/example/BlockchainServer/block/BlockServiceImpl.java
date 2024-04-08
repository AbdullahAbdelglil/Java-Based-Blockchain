package com.example.BlockchainServer.block;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockServiceImpl implements BlockService{
    private final BlockRepository blockRepository;

    @Autowired
    public BlockServiceImpl(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public void save(Block block) {
        blockRepository.save(block);
    }

    public List<Block> getBlockchain() {
        return blockRepository.findAll();
    }

    public Block getLastBlock() {
        List<Block> blocks = getBlockchain();
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks.get(blocks.size() - 1);
    }

    public void removeLastBlock() {
        Block block = getLastBlock();
        blockRepository.deleteByHash(block.getHash());
    }

}
