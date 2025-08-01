import { ConversationListItem, getConversationList } from "@/api"
import Logo from "@/assets/images/logo.png"
import NoData from '@/assets/images/nodata.png'
import Card from "@/components/Card"
import { AppType } from "@/constant/enums"
import { tableSx } from "@/constant/styles"
import { useURLSearchParams } from "@/hooks"
import { useAppSelector } from "@/store"
import { Box, Stack } from "@mui/material"
import { Ellipsis, Icon, Table } from "ct-mui"
import dayjs from "dayjs"
import { useEffect, useState } from "react"
import Detail from "./Detail"
import Search from "./Search"

const Conversation = () => {
  const { kb_id = '' } = useAppSelector(state => state.config)
  const [searchParams] = useURLSearchParams()
  const subject = searchParams.get('subject') || ''
  const remoteIp = searchParams.get('remote_ip') || ''
  const [data, setData] = useState<ConversationListItem[]>([])
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [total, setTotal] = useState(0)
  const [open, setOpen] = useState(false)
  const [id, setId] = useState('')

  const appTypes = Object.values(AppType).map(it => it.label)

  const columns = [
    {
      dataIndex: 'subject',
      title: '问题',
      render: (text: string, record: ConversationListItem) => {
        const isGroupChat = record.info?.user_info?.from === 1
        return <Stack direction={'row'} alignItems={'center'} gap={0.5}>
          <Icon type={isGroupChat ? 'icon-qunliao' : 'icon-danliao'} />
          <Ellipsis className="primary-color" sx={{ cursor: 'pointer', flex: 1, width: 0 }} onClick={() => {
            setId(record.id)
            setOpen(true)
          }}>
            {text}
          </Ellipsis>
        </Stack>
      },
    },
    {
      dataIndex: 'app_type',
      title: '来源渠道',
      width: 160,
      render: (text: number) => {
        const typeName = ['', ...appTypes]
        return typeName[text] || '-'
      }
    },
    {
      dataIndex: 'info',
      title: '来源用户',
      width: 200,
      render: (text: ConversationListItem['info']) => {
        const user = text?.user_info
        return <Box sx={{ fontSize: 12 }}>
          <Stack direction={'row'} alignItems={'center'} gap={0.5} sx={{ cursor: 'pointer' }}>
            <img src={user?.avatar || Logo} width={16} />
            <Box sx={{ fontSize: 14 }}>{user?.real_name || user?.name || '匿名用户'}</Box>
          </Stack>
          {user?.email && <Box sx={{ color: 'text.auxiliary' }}>{user?.email}</Box>}
        </Box>
      }
    },
    {
      dataIndex: 'remote_ip',
      title: '来源 IP',
      width: 200,
      render: (text: string, record: ConversationListItem) => {
        const { city = '', country = '', province = '' } = record.ip_address
        return <>
          <Box>{text}</Box>
          <Box sx={{ color: 'text.auxiliary', fontSize: 12 }}>{country === '中国' ? `${province}-${city}` : `${country}`}</Box>
        </>
      }
    },
    {
      dataIndex: 'created_at',
      title: '问答时间',
      width: 170,
      render: (text: string) => {
        return dayjs(text).fromNow()
      }
    },
  ]

  const getData = () => {
    setLoading(true)
    getConversationList({ page, per_page: pageSize, kb_id, subject, remote_ip: remoteIp }).then(res => {
      setData(res.data)
      setTotal(res.total)
    }).finally(() => {
      setLoading(false)
    })
  }

  useEffect(() => {
    setPage(1)
  }, [subject, remoteIp, kb_id])

  useEffect(() => {
    if (kb_id) getData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, pageSize, subject, remoteIp, kb_id])

  return <Card>
    <Stack direction='row' alignItems={'center'} justifyContent={'space-between'} sx={{ p: 2 }}>
      <Search />
    </Stack>
    <Table
      columns={columns}
      dataSource={data}
      rowKey="id"
      height="calc(100vh - 148px)"
      size='small'
      sx={{
        overflow: 'hidden',
        ...tableSx,
        '.MuiTableContainer-root': {
          height: 'calc(100vh - 148px - 70px)',
        }
      }}
      pagination={{
        total,
        page,
        pageSize,
        onChange: (page, pageSize) => {
          setPage(page)
          setPageSize(pageSize)
        },
      }}
      PaginationProps={{
        sx: {
          borderTop: '1px solid',
          borderColor: 'divider',
          p: 2,
          '.MuiSelect-root': {
            width: 100,
          }
        }
      }}
      renderEmpty={loading ? <Box></Box> : <Stack alignItems={'center'} sx={{ mt: 20 }}>
        <img src={NoData} width={174} />
        <Box>暂无数据</Box>
      </Stack>}
    />
    <Detail id={id} open={open} onClose={() => {
      setOpen(false)
    }} />
  </Card>
}

export default Conversation
