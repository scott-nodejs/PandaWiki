import { createNode, updateNode } from "@/api"
import Emoji from "@/components/Emoji"
import { useAppSelector } from "@/store"
import { Box, TextField } from "@mui/material"
import { Message, Modal } from "ct-mui"
import { useEffect } from "react"
import { Controller, useForm } from "react-hook-form"

interface DocAddByCustomTextProps {
  open: boolean
  data?: { id: string, name: string, emoji: string } | null
  onClose: () => void
  refresh?: () => void
  type?: 1 | 2
}
const DocAddByCustomText = ({ open, data, onClose, refresh, type = 2 }: DocAddByCustomTextProps) => {
  const { kb_id: id } = useAppSelector(state => state.config)
  const text = type === 1 ? '文件夹' : '文档'

  const { control, handleSubmit, reset, formState: { errors } } = useForm<{ name: string, emoji: string }>({
    defaultValues: {
      name: '',
      emoji: '',
    }
  })

  const handleClose = () => {
    reset()
    onClose()
  }

  const submit = (value: { name: string, emoji: string }) => {
    if (data) {
      updateNode({ id: data.id, kb_id: id, name: value.name, emoji: value.emoji }).then(() => {
        Message.success('修改成功')
        reset()
        handleClose()
        refresh?.()
      })
    } else {
      if (!id) return
      createNode({ name: value.name, content: '', kb_id: id, parent_id: null, type, emoji: value.emoji }).then(({ id }) => {
        Message.success('创建成功')
        reset()
        handleClose()
        refresh?.()
        if (type === 2) {
          window.open(`/doc/editor/${id}`, '_blank')
        }
      })
    }
  }

  useEffect(() => {
    // 只在弹窗打开状态变化时重置表单
    if (open) {
      if (data) {
        reset({
          name: data.name || '',
          emoji: data.emoji || '',
        })
      } else {
        reset({
          name: '',
          emoji: '',
        })
      }
    }
  }, [open, data?.id]) // 只依赖 open 和 data?.id

  return <Modal
    title={data ? `编辑${text}` : `创建${text}`}
    open={open}
    width={600}
    okText={data ? '保存' : '创建'}
    onCancel={handleClose}
    onOk={handleSubmit(submit)}
  >
    <Box sx={{ fontSize: 14, lineHeight: '36px' }}>
      {text}名称
    </Box>
    <Controller
      control={control}
      name="name"
      rules={{ required: `请输入${text}名称` }}
      render={({ field }) => (
        <TextField
          {...field}
          fullWidth
          autoFocus
          size="small"
          placeholder={`请输入${text}名称`}
          error={!!errors.name}
          helperText={errors.name?.message}
        />
      )}
    />
    <Box sx={{ fontSize: 14, lineHeight: '36px', mt: 1 }}>
      {text}图标
    </Box>
    <Controller
      control={control}
      name="emoji"
      render={({ field }) => (
        <Emoji
          {...field}
          type={type}
        />
      )}
    />
  </Modal>
}


export default DocAddByCustomText