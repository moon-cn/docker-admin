import React from 'react';
import CodeMirror from 'codemirror';
import 'codemirror/lib/codemirror.css'

// 主题
import 'codemirror/theme/darcula.css'

// 支持格式
import 'codemirror/mode/css/css.js'
import 'codemirror/mode/yaml/yaml';
import 'codemirror/mode/shell/shell'
import 'codemirror/mode/dockerfile/dockerfile'

// 数据校验
import 'codemirror/addon/lint/lint.css'
import 'codemirror/addon/lint/yaml-lint.js'


class CodeMirrorEditor extends React.Component {
  constructor(props) {
    super(props);
    this.textareaRef = React.createRef();
    this.editor = null;
  }

  componentDidMount() {
    setTimeout(this.init, 500)
  }

  init = () => {
    const textarea = this.textareaRef.current;
    this.editor = CodeMirror.fromTextArea(textarea, {
      mode:   this.props.mode || 'yaml',
      lineNumbers: true,
      tabSize: 2,
      theme: "darcula"
    });

    this.editor.on('change', (cm) => {
      this.props.onChange(cm.getValue());
    });
  };

  componentWillUnmount() {
    if(this.editor){
      this.editor.toTextArea();
    }
  }

  render() {
    return (
      <textarea
        style={{width:100,height:100}}
        ref={this.textareaRef}
        defaultValue={this.props.value}
      />
    );
  }
}

export default CodeMirrorEditor;
