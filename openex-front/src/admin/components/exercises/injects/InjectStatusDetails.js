import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import * as R from 'ramda';
import { Dialog, DialogContent, DialogActions, Button, IconButton, TableRow, TableCell, TableBody, Table } from '@mui/material';
import { PreviewOutlined } from '@mui/icons-material';
import Transition from '../../../../components/common/Transition';
import inject18n from '../../../../components/i18n';

class InjectStatusDetails extends Component {
  constructor(props) {
    super(props);
    this.state = { open: false };
  }

  handleOpen() {
    this.setState({ open: true });
  }

  handleClose() {
    this.setState({ open: false });
  }

  render() {
    const { t, status } = this.props;
    return (
      <div>
        <IconButton
          onClick={this.handleOpen.bind(this)}
          aria-haspopup="true"
          size="large"
        >
          <PreviewOutlined />
        </IconButton>
        {status && (
          <Dialog
            open={this.state.open}
            TransitionComponent={Transition}
            onClose={this.handleClose.bind(this)}
            fullWidth={true}
            maxWidth="md"
            PaperProps={{ elevation: 1 }}
          >
            <DialogContent>
              <Table selectable={false} size="small">
                <TableBody displayRowCheckbox={false}>
                  {Object.entries(status.status_reporting).map(
                    ([key, value]) => {
                      if (key === 'execution_traces') {
                        return (
                          <TableRow key={key}>
                            <TableCell>{key}</TableCell>
                            <TableCell>
                              <Table selectable={false} size="small" key={key}>
                                <TableBody displayRowCheckbox={false}>
                                  {value.map((trace) => (
                                    <TableRow key={trace.trace_identifier}>
                                      <TableCell>
                                        {trace.trace_message}
                                      </TableCell>
                                      <TableCell>
                                        {trace.trace_status}
                                      </TableCell>
                                      <TableCell>{trace.trace_time}</TableCell>
                                    </TableRow>
                                  ))}
                                </TableBody>
                              </Table>
                            </TableCell>
                          </TableRow>
                        );
                      }
                      return (
                        <TableRow key={key}>
                          <TableCell>{key}</TableCell>
                          <TableCell>{value}</TableCell>
                        </TableRow>
                      );
                    },
                  )}
                </TableBody>
              </Table>
            </DialogContent>
            <DialogActions>
              <Button onClick={this.handleClose.bind(this)}>
                {t('Close')}
              </Button>
            </DialogActions>
          </Dialog>
        )}
      </div>
    );
  }
}

InjectStatusDetails.propTypes = {
  classes: PropTypes.object,
  t: PropTypes.func,
  status: PropTypes.object,
};

export default R.compose(inject18n)(InjectStatusDetails);
