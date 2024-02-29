
const getTableFormProps = (initValues) => {
  return {
    form: {
      layout: 'horizontal',
      labelCol: {flex:'100px'},
      initialValues: initValues
    },
    type: "form",
    rowSelection: false,

  }
}
const common = {
  getTableFormProps
}
export default common
