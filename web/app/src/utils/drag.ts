import { ITreeItem, NodeListItem } from "@/assets/type";

/**
 * 处理后端返回的树形数据，转换为前端需要的ITreeItem格式
 */
export function convertFromTreeData(data: NodeListItem[]): ITreeItem[] {
  const convertNode = (node: NodeListItem, level: number = 0): ITreeItem => {
    const converted: ITreeItem = {
      id: node.id,
      summary: node.summary ?? undefined, // 将null转换为undefined
      name: node.name,
      level,
      status: node.status,
      visibility: node.visibility,
      order: node.sort, // 使用sort字段
      emoji: node.emoji,
      type: node.type,
      parentId: node.parent_id,
      children: [],
      canHaveChildren: node.type === 1,
      updated_at: node.updated_at || node.created_at,
    };

    // 递归处理子节点
    if (node.children && node.children.length > 0) {
      converted.children = node.children
        .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0)) // 按sort字段排序
        .map(child => convertNode(child, level + 1));
    }

    return converted;
  };

  return data
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0)) // 按sort字段排序
    .map(node => convertNode(node));
}

/**
 * 测试数据转换函数
 */
export function testDataConversion() {
  // 模拟后端返回的树形数据
  const mockTreeData: NodeListItem[] = [
    {
      "id": "8921fdc3acdb46afb5eed5f5ac2041ca",
      "name": "wwwwwwww",
      "type": 1,
      "emoji": "",
      "summary": null,
      "sort": 0,
      "status": 2,
      "visibility": 2,
      "children": [
        {
          "id": "3c5f1d7625554bdf9872597b8748917f",
          "name": "eeeeeeeee",
          "type": 2,
          "emoji": "",
          "summary": null,
          "sort": 0,
          "status": 2,
          "visibility": 2,
          "children": null,
          "parent_id": "8921fdc3acdb46afb5eed5f5ac2041ca",
          "kb_id": "7333663447fc415c8ac4349bbf0876e3",
          "created_at": "2025-07-23 17:35:14",
          "updated_at": "2025-07-23 20:13:10"
        },
        {
          "id": "eb1e7e1d127f4ada8ceac7a0715040c8",
          "name": "eeeeeeeeeeeeeee",
          "type": 2,
          "emoji": "",
          "summary": null,
          "sort": 0,
          "status": 2,
          "visibility": 2,
          "children": null,
          "parent_id": "8921fdc3acdb46afb5eed5f5ac2041ca",
          "kb_id": "7333663447fc415c8ac4349bbf0876e3",
          "created_at": "2025-07-23 17:35:24",
          "updated_at": "2025-07-23 17:35:24"
        }
      ],
      "parent_id": null,
      "kb_id": "7333663447fc415c8ac4349bbf0876e3",
      "created_at": "2025-07-23 17:33:29",
      "updated_at": "2025-07-23 17:33:29"
    }
  ];

  console.log('Original data:', mockTreeData);
  const converted = convertFromTreeData(mockTreeData);
  console.log('Converted data:', converted);
  
  return converted;
}

export function convertToTree(data: NodeListItem[]) {
  const nodeMap = new Map<string, ITreeItem>();
  const rootNodes: ITreeItem[] = [];

  // 第一次遍历：创建所有节点
  data.forEach(item => {
    const node: ITreeItem = {
      id: item.id,
      summary: item.summary ?? undefined, // 将null转换为undefined
      name: item.name,
      level: 0,
      status: item.status,
      visibility: item.visibility,
      order: item.sort, // 使用sort字段而不是position
      emoji: item.emoji,
      type: item.type,
      parentId: item.parent_id || null,
      children: [],
      canHaveChildren: item.type === 1,
      updated_at: item.updated_at || item.created_at,
    };

    nodeMap.set(item.id, node);
  });

  // 第二次遍历：构建树结构
  nodeMap.forEach(node => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      const parent = nodeMap.get(node.parentId)!;
      node.level = parent.level + 1;
      parent.children!.push(node);
    } else {
      rootNodes.push(node);
    }
  });

  // 对所有层级的节点进行排序
  const sortChildren = (nodes: ITreeItem[]) => {
    nodes.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
    nodes.forEach(node => {
      if (node.children?.length) {
        sortChildren(node.children);
      }
    });
  };

  sortChildren(rootNodes);
  return rootNodes;
}

export const filterEmptyFolders = (data: ITreeItem[]): ITreeItem[] => {
  return data
    .map(item => {
      if (item.children && item.children.length > 0) {
        const filteredChildren = filterEmptyFolders(item.children)
        return { ...item, children: filteredChildren }
      }
      return item
    })
    .filter(item => {
      if (item.type === 1) {
        return item.children && item.children.length > 0
      }
      return true
    })
}

export const addExpandState = (nodes: ITreeItem[], activeId: string, defaultExpand: boolean): ITreeItem[] => {
  const findParentPath = (nodes: ITreeItem[], targetId: string, path: string[] = []): string[] | null => {
    for (const node of nodes) {
      if (node.id === targetId) {
        return path;
      }
      if (node.children && node.children.length > 0) {
        const found = findParentPath(node.children, targetId, [...path, node.id]);
        if (found) return found;
      }
    }
    return null;
  };

  const parentPath = findParentPath(nodes, activeId) || [];
  const parentSet = new Set(parentPath);

  const addExpand = (nodes: ITreeItem[]): ITreeItem[] => {
    return nodes.map(node => {
      const isExpanded = parentSet.has(node.id) ? true : defaultExpand;
      if (node.children && node.children.length > 0) {
        return {
          ...node,
          defaultExpand: isExpanded,
          children: addExpand(node.children)
        };
      }
      return node;
    });
  };

  return addExpand(nodes);
};