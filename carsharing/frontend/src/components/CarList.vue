<template>

<v-data-table
    :headers="headers"
    :items="carList"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'CarList',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "carId", value: "carId" },
            { text: "status", value: "status" },
        ],
        carList : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/carlists')

      this.carList = temp.data._embedded.carlists;

    },
    methods: {
    }
  }
</script>

