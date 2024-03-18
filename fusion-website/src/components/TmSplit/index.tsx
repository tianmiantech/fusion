import React from 'react';
import Split from 'react-split';
import classnames from 'classnames';
import './index.less';

const gutterStyle = () => ({
  'flex-basis': '10px',
  'z-index': 8, // 提升层级
});

const TmSplit = props => {
  const { className, direction, children, ...rest } = props;
  const cx = classnames('tm-wrapper-split', className, {
    vertical: direction === 'vertical',
  });
  return (
    <Split className={cx} direction={direction} {...rest}>
      {children}
    </Split>
  );
};

TmSplit.defaultProps = {
  direction: 'horizontal',
  sizes: [15, 85],
  minSize: 100,
  gutterStyle,
};

export default TmSplit;
