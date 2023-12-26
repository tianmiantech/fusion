import React, { useEffect, useState } from 'react';
import { Form, Select, Popover, Button, Space, Modal, Card, List, Descriptions } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';
import { useMount,useRequest } from 'ahooks';
import lodash from 'lodash'
import useBloomFilterFormItem from "@/pages/job/hooks/useBloomFilterFormItem"
import type { SuggestListItemInterface } from "@/pages/job/hooks/useBloomFilterFormItem";
import { useImmer } from 'use-immer';
import styles from './index.less';
import {getNoramlTime} from '@/utils/time'
import {getFileSizeInHumanReadable} from '@/utils/file'
import {getBloomFilterDetailById} from '../../service'
import {renderHashConfig} from '@/utils/utils'

interface BloomFilterDataInterface {
  isModalOpen:boolean,
  selectedBloomFilterConfig:SuggestListItemInterface
}
interface BloomFilterFormItemPropsInterface {
  onBloomFilterSelectedCallBack:(value:SuggestListItemInterface)=>void
  value?:string,
  onChange?:(value:any)=>void
}

const  BloomFilterFormItem = (props:BloomFilterFormItemPropsInterface) => {
    const {onBloomFilterSelectedCallBack,onChange,value} = props

    const {bloomFilterConfig,checkBloomFilterList} = useBloomFilterFormItem();

    const [data,setData] = useImmer<BloomFilterDataInterface>({
      isModalOpen:false,
      selectedBloomFilterConfig:{
        id:'',
      }
    })

    //编辑时，Form主动设置value，将数据进行回填
    useEffect(()=>{
      if(value){
        const source = lodash.get(value,'source','')
        if(source === "setFieldsValue") {
         const bloom_filter_id = lodash.get(value,'bloom_filter_id','')
         if(bloom_filter_id){
          runGetBloomFilterDetailById(bloom_filter_id)
         }
        }
      }
    },[value])

    //编辑时获取所有的数据详情
    const {run:runGetBloomFilterDetailById} = useRequest(async (id:string)=>{
      const response = await getBloomFilterDetailById(id)
      const {code,data} = response
      if(code === 0){
        setData(g=>{
          g.selectedBloomFilterConfig = data
        })
      }
    
    },{manual:true})



    useMount(()=>{
        checkBloomFilterList();
    })

    const itemStyle = {
      paddingBottom:5
    }

    const onCardClick = (item:SuggestListItemInterface) => {
      setData(g=>{
        g.selectedBloomFilterConfig = item
        g.isModalOpen = false
      })
      const hash_config = lodash.get(item,'hash_config')
      onBloomFilterSelectedCallBack && onBloomFilterSelectedCallBack(hash_config)
      onChange && onChange({bloom_filter_id:item.id})
    }

    const renderCard = (item:SuggestListItemInterface,showSelected:boolean=true) => {
      const id = lodash.get(data,'selectedBloomFilterConfig.id','')
      return <Card title={item.description}  bodyStyle={{padding:10}} className={`${styles.cardStyle} ${id ===item.id && showSelected?styles.cardStyleSelected:''}`} onClick={()=>{onCardClick(item)}}>
          <Descriptions column={2}>
            <Descriptions.Item span={2} label="主键" style={itemStyle}>{renderHashConfig(lodash.get(item,'hash_config'))}</Descriptions.Item>
            <Descriptions.Item span={2} label='创建时间' style={itemStyle}>{getNoramlTime(lodash.get(item,'created_time'))}</Descriptions.Item>
            <Descriptions.Item label='数据量' style={itemStyle}>{item.total_data_count}</Descriptions.Item>
            <Descriptions.Item label='文件大小' style={itemStyle}>{getFileSizeInHumanReadable(lodash.get(item,'storage_size'))}</Descriptions.Item>
          </Descriptions>
      </Card>
    }

    const renderListItem = (item:SuggestListItemInterface) => {     
      return <List.Item>
              {renderCard(item)}
            </List.Item>
    }


    const renderPopoverContent = () => {
      return <div style={{width:400}}>{renderCard(data.selectedBloomFilterConfig,false)}</div>
    }

    const openModel = (value:boolean) => {
      setData(g=>{
        g.isModalOpen = value
      })
    }

    const renderButton = () => {
      const id = lodash.get(data,'selectedBloomFilterConfig.id','')
      if(id){
        const description = lodash.get(data,'selectedBloomFilterConfig.description','')
        return <>
          <Popover content={renderPopoverContent}>
            <Button  onClick={()=>{openModel(true)}}>{description}</Button>
          </Popover>
        </>
      } 
      return <>
        <Button onClick={()=>{openModel(true)}}>选择布隆过滤器</Button>
        </>
    }

    return <>
        <Space>
          {renderButton()}
        </Space>
        <Modal title={'选择布隆过滤器'} open={data.isModalOpen} width={1200} onCancel={()=>openModel(false)}>
        <List
          grid={{
            gutter: 16,
            xs: 1,
            sm: 2,
            md: 4,
            lg: 4,
            xl: 6,
            xxl: 3,
          }}
          dataSource={bloomFilterConfig.suggestList}
          renderItem={renderListItem}
          />
        </Modal>
    </>
}
export default BloomFilterFormItem;