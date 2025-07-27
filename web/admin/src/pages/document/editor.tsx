import { getNodeDetail, NodeDetail, updateNode, uploadFile } from "@/api";
import { useAppDispatch } from "@/store";
import { setKbId } from "@/store/slices/config";
import { Box, Stack, useMediaQuery } from "@mui/material";
import { Message } from "ct-mui";
import { TiptapEditor, TiptapToolbar, useTiptapEditor } from 'ct-tiptap-editor';
import dayjs, { Dayjs } from "dayjs";
import { useEffect, useRef, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import VersionPublish from "../release/components/VersionPublish";
import EditorDocNav from "./component/EditorDocNav";
import EditorFolder from "./component/EditorFolder";
import EditorHeader from "./component/EditorHeader";
import EditorSummary from "./component/EditorSummary";

const DocEditor = () => {
  const timer = useRef<NodeJS.Timeout | null>(null)
  const updateTimer = useRef<NodeJS.Timeout | null>(null)
  const { id = '' } = useParams()
  const dispatch = useAppDispatch()
  const isWideScreen = useMediaQuery('(min-width:1400px)')
  const [edited, setEdited] = useState(false)
  const [detail, setDetail] = useState<NodeDetail | null>(null)
  const [updateAt, setUpdateAt] = useState<Dayjs | null>(null)
  const [headings, setHeadings] = useState<{ id: string, title: string, heading: number }[]>([])
  const [maxH, setMaxH] = useState(0)
  const [publishOpen, setPublishOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const getDetail = () => {
    getNodeDetail({ id }).then(res => {
      setDetail(res)
      setEdited(false)
      dispatch(setKbId(res.kb_id))
      // 安全地解析时间，如果失败则使用当前时间
      try {
        setUpdateAt(res.updated_at ? dayjs(res.updated_at) : dayjs())
      } catch (error) {
        console.warn('Failed to parse updated_at:', res.updated_at, error)
        setUpdateAt(dayjs())
      }
    }).catch(error => {
      console.error('Failed to get node detail:', error)
      Message.error('获取文档详情失败')
    })
  }

  // 使用useCallback来稳定函数引用，避免闭包问题
  const getDetailCallback = useCallback(() => {
    if (!id) return
    setLoading(true)
    getNodeDetail({ id }).then(res => {
      setDetail(res)
      setEdited(false)
      dispatch(setKbId(res.kb_id))
      // 安全地解析时间，如果失败则使用当前时间
      try {
        setUpdateAt(res.updated_at ? dayjs(res.updated_at) : dayjs())
      } catch (error) {
        console.warn('Failed to parse updated_at:', res.updated_at, error)
        setUpdateAt(dayjs())
      }
    }).catch(error => {
      console.error('Failed to get node detail:', error)
      Message.error('获取文档详情失败')
    }).finally(() => {
      setLoading(false)
    })
  }, [id, dispatch])

  const handleUpload = async (
    file: File,
    onProgress?: (progress: { progress: number }) => void,
    abortSignal?: AbortSignal
  ) => {
    const formData = new FormData()
    formData.append('file', file)
    const { key } = await uploadFile(formData, {
      onUploadProgress: (event) => {
        onProgress?.(event)
      },
      abortSignal
    })
    return Promise.resolve('/static-file/' + key)
  }

  const editorRef = useTiptapEditor({
    content: '',
    immediatelyRender: true,
    size: 100,
    aiUrl: '/api/v1/creation/text',
    onUpload: handleUpload,
    onSave: (html) => {
      // 避免循环引用，直接在这里处理保存逻辑
      if (!editorRef || !detail) return
      const content = html || editorRef.getHtml()
      updateNode({ id, content, kb_id: detail.kb_id }).then(() => {
        Message.success('保存成功')
        setEdited(false)
        setUpdateAt(dayjs())
      }).catch(() => {
        Message.error('保存失败')
      })
    },
    onUpdate: () => {
      // 添加防抖，避免频繁触发状态更新
      if (updateTimer.current) clearTimeout(updateTimer.current)
      updateTimer.current = setTimeout(() => {
        setEdited(true)
      }, 100) // 100ms防抖
    },
    onError: (error: Error) => {
      Message.error(error.message)
    }
  })

  // 在editorRef定义之后定义handleSave
  const handleSave = useCallback(async (auto?: boolean, publish?: boolean, html?: string) => {
    if (!editorRef || !detail) return
    const content = html || editorRef.getHtml()
    try {
      await updateNode({ id, content, kb_id: detail.kb_id })
      if (auto === true) Message.success('自动保存成功')
      else if (auto === undefined) Message.success('保存成功')
      setEdited(false)
      if (publish) setPublishOpen(true)
      setUpdateAt(dayjs())
      // 更新导航
      if (editorRef) {
        const headings = await editorRef.getNavs() || []
        setHeadings(headings)
        setMaxH(Math.min(...headings.map((h: any) => h.heading)))
      }
    } catch (error) {
      Message.error('保存失败')
    }
  }, [editorRef, detail, id])

  // 处理编辑器内容设置，避免在用户输入时重复设置内容
  useEffect(() => {
    if (timer.current) clearInterval(timer.current)
    
    // 只有在编辑器存在、有详情数据，且当前没有编辑状态时才设置内容
    if (detail && editorRef && detail.content !== undefined && !edited) {
      // 检查当前编辑器内容是否与服务端内容不同，避免不必要的设置
      const currentContent = editorRef.getHtml() || ''
      const serverContent = detail.content || ''
      
      if (currentContent !== serverContent) {
        console.log('Setting content from server:', { currentContent, serverContent })
        // 使用防抖，避免频繁调用
        const timeoutId = setTimeout(() => {
          editorRef.setContent(serverContent).then((headings) => {
            setHeadings(headings)
            setMaxH(Math.min(...headings.map(h => h.heading)))
          }).catch(error => {
            console.error('Failed to set content:', error)
          })
        }, 100)
        
        return () => {
          clearTimeout(timeoutId)
        }
      }
      
      // 设置自动保存定时器（不管内容是否相同都需要设置）
      timer.current = setInterval(() => {
        if (editorRef && detail && edited) { // 只有编辑过才自动保存
          const content = editorRef.getHtml()
          updateNode({ id, content, kb_id: detail.kb_id }).then(() => {
            Message.success('自动保存成功')
            setEdited(false)
            setUpdateAt(dayjs())
          }).catch(() => {
            Message.error('保存失败')
          })
        }
      }, 1000 * 60)
    }
    
    return () => {
      if (updateTimer.current) clearInterval(updateTimer.current)
    }
  }, [detail?.id, editorRef, edited]) // 移除 detail?.content 依赖，添加 edited 状态

  // 创建ref来存储最新状态，避免闭包问题
  const latestStateRef = useRef({ edited, editorRef, detail, id })
  useEffect(() => {
    latestStateRef.current = { edited, editorRef, detail, id }
  })

  // 处理页面路由变化，重置状态并获取新数据
  useEffect(() => {
    if (id) {
      if (timer.current) clearInterval(timer.current)
      // 重置状态
      setDetail(null)
      setEdited(false)
      setLoading(true)
      
      // 使用稳定的回调函数获取数据
      getDetailCallback()
      
      setTimeout(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }, 60)
    }

    const handleVisibilityChange = () => {
      const { edited, editorRef, detail, id } = latestStateRef.current
      if (document.hidden && edited && editorRef && detail) {
        // 直接调用保存逻辑，使用最新状态
        const content = editorRef.getHtml()
        updateNode({ id, content, kb_id: detail.kb_id }).then(() => {
          Message.success('自动保存成功')
          setEdited(false)
          setUpdateAt(dayjs())
        }).catch(() => {
          Message.error('保存失败')
        })
      }
    }
    document.addEventListener('visibilitychange', handleVisibilityChange)
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [id, getDetailCallback]) // 只依赖 id 和稳定的 getDetailCallback

  // 当从窄屏切换到宽屏时，如果还没有数据则请求
  useEffect(() => {
    if (isWideScreen && id && !detail && !loading) {
      getDetailCallback()
    }
  }, [isWideScreen, id, detail, loading, getDetailCallback])

  // 如果正在加载或编辑器未初始化，显示loading状态
  if (!editorRef || loading) {
    return <Box sx={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      color: 'text.primary'
    }}>
      加载中...
    </Box>
  }

  return <Box sx={{ color: 'text.primary', pb: 2 }}>
    {/* 固定头部 */}
    <Box sx={{
      position: 'fixed',
      top: 0,
      width: '100vw',
      zIndex: 1000,
      bgcolor: '#fff',
      boxShadow: '0px 0px 10px 0px rgba(0, 0, 0, 0.1)',
    }}>
      <Box sx={{
        borderBottom: '1px solid',
        borderColor: 'divider',
        py: 1,
      }}>
        <EditorHeader
          edited={edited}
          detail={detail}
          editorRef={editorRef}
          updateAt={updateAt}
          onSave={(auto, publish) => handleSave(auto, publish)}
          refresh={async () => {
            await handleSave(false)
            getDetail()
          }} />
      </Box>
      <Box sx={{
        width: 900,
        margin: 'auto',
      }}>
        <TiptapToolbar editorRef={editorRef} />
      </Box>
    </Box>

    {/* 三栏布局容器 */}
    <Box sx={{
      pt: '105px',
      display: 'flex',
      justifyContent: 'center',
      gap: isWideScreen ? 1 : 0, // 8px间隔
    }}>
      {/* 左侧边栏 */}
      {isWideScreen && (
        <Box sx={{
          width: 292,
          position: 'fixed',
          left: 'calc(50vw - 700px - 4px)', // 居中定位：视口中心 - 总宽度一半 - 间隔一半
          top: '105px',
          height: 'calc(100vh - 105px)',
          overflowY: 'auto',
          zIndex: 1,
        }}>
          <EditorFolder
            edited={edited}
            save={handleSave}
          />
        </Box>
      )}

      {/* 中间内容区域 */}
      <Box className='editor-content' sx={{
        width: 800,
        overflowY: 'auto',
        position: 'relative',
        zIndex: 1,
        m: '0 auto',
        '.editor-container': {
          p: 4,
          borderRadius: '6px',
          bgcolor: '#fff',
          '.tiptap': {
            minHeight: 'calc(100vh - 185px)',
          }
        }
      }}>
        <TiptapEditor editorRef={editorRef} />
      </Box>

      {/* 右侧边栏 */}
      {isWideScreen && (
        <Box sx={{
          width: 292,
          position: 'fixed',
          right: 'calc(50vw - 700px - 4px)', // 居中定位：视口中心 - 总宽度一半 - 间隔一半
          top: '105px',
          height: 'calc(100vh - 105px)',
          overflowY: 'auto',
          zIndex: 1,
        }}>
          <Stack gap={1}>
            <EditorSummary
              kb_id={detail?.kb_id || ''}
              id={detail?.id || ''}
              name={detail?.name || ''}
              summary={detail?.summary || detail?.meta?.summary || ''}
            />
            <EditorDocNav
              title={detail?.name}
              headers={headings}
              maxH={maxH}
            />
          </Stack>
        </Box>
      )}
    </Box>

    <VersionPublish
      open={publishOpen}
      defaultSelected={[id]}
      onClose={() => setPublishOpen(false)}
      refresh={() => getDetail()}
    />
  </Box>
}

export default DocEditor