'use client'

import { IconFold, IconUnfold } from "@/components/icons";
import { useStore } from "@/provider";
import { addExpandState, convertToTree, convertFromTreeData, filterEmptyFolders } from "@/utils/drag";
import { Box, IconButton } from "@mui/material";
import CatalogFolder from "./CatalogFolder";

const Catalog = ({ id, setId }: { id?: string, setId?: (id: string) => void }) => {
  const { kbDetail, nodeList = [], mobile = false, catalogShow, setCatalogShow } = useStore()
  if (mobile) return null
  const catalogSetting = kbDetail?.settings?.catalog_settings
  const catalogFolderExpand = catalogSetting?.catalog_folder !== 2
  
  // 检查数据是否已经是树形结构
  const hasChildren = nodeList.some(node => node.children && node.children.length > 0)
  const treeData = hasChildren ? convertFromTreeData(nodeList) : convertToTree(nodeList)
  const tree = addExpandState(filterEmptyFolders(treeData || []), id as string, catalogFolderExpand)

  return <>
    <Box sx={{
      color: 'text.primary',
      position: 'fixed',
      zIndex: 11,
      top: 18,
      left: catalogShow ? ((catalogSetting?.catalog_width ?? 260) - 16) : 0,
    }}>
      <IconButton size='small' sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: '50%',
        width: 32,
        height: 32,
        color: 'text.primary',
        bgcolor: 'background.paper',
        '&:hover': {
          bgcolor: 'background.paper',
          borderColor: 'divider',
        }
      }} onClick={() => setCatalogShow?.(!catalogShow)}>
        {catalogShow ? <IconFold /> : <IconUnfold />}
      </IconButton>
    </Box>
    {!catalogShow ? <Box sx={{
      width: 16,
      height: '100vh',
      borderRight: '1px solid',
      borderColor: 'divider',
      position: 'fixed',
      zIndex: 5,
    }}></Box> : <Box sx={{
      width: catalogSetting?.catalog_width ?? 260,
      px: 2,
      py: 3,
      fontSize: 14,
      position: 'fixed',
      zIndex: 5,
      borderRight: '1px solid',
      borderColor: 'divider',
      lineHeight: '22px',
      color: 'text.primary',
    }}>
      <Box sx={{
        px: 2,
        pb: 1,
        lineHeight: '22px',
        fontWeight: 'bold',
      }}>目录</Box>
      <Box sx={{
        height: 'calc(100vh - 78px)',
        overflowY: 'auto',
        overflowX: 'hidden',
        '&::-webkit-scrollbar': {
          display: 'none',
        },
        msOverflowStyle: 'none',
        scrollbarWidth: 'none',
      }}>
        {tree.map((item) => <CatalogFolder id={id} key={item.id} item={item} setId={setId} />)}
      </Box>
    </Box>}
  </>
};

export default Catalog;