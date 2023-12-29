import React, { useRef, useEffect, useState } from 'react';
import { Tooltip } from 'antd';

interface TruncatedStringProps {
  text: string;
  style?: {};
}

const TruncatedString: React.FC<TruncatedStringProps> = ({ text,style }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [maxLength, setMaxLength] = useState<number>(text.length);

  useEffect(() => {
    const containerWidth = containerRef.current?.offsetWidth || 0;
    const textWidth = getTextWidth(text);
    
    // 设置最大长度，使字符串不超过父元素的宽度
    if (textWidth > containerWidth) {
      const newMaxLength = Math.floor((containerWidth / textWidth) * text.length);
      setMaxLength(newMaxLength);
    } else {
      setMaxLength(text.length);
    }
  }, [text]);

  const getTextWidth = (str: string): number => {
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    if (context) {
      context.font = 'inherit';
      const width = context.measureText(str).width;
      return width;
    }
    return 0;
  };

  const truncatedText = text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
  console.log(text.length, maxLength);
  
  return (
    <Tooltip title={text.length > maxLength ? text : undefined}>
      <span ref={containerRef}  style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',...style }}>
        {truncatedText}
      </span>
    </Tooltip>
  );
};

export default TruncatedString;
